package com.ajoclub.ajopos.view.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ajoclub.ajopos.bt.BluetoothPrinterManager
import com.ajoclub.ajopos.bt.ReceiptFormatter
import com.ajoclub.ajopos.databinding.FragmentDeviceManagerBinding
import com.ajoclub.ajopos.util.PrinterPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceManagerFragment : Fragment() {

    private var _binding: FragmentDeviceManagerBinding? = null
    private val binding get() = _binding!!

    private val REQ_PERM_BT = 3001

    private val btPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- BACK BUTTON ---


        // Init UI
        updateBluetoothStatus()
        loadPrinters()
        loadPairedDevicesToList()
        applyPrinterModeLock()

        // Toggle Bluetooth
        binding.btnToggleBluetooth.setOnClickListener {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter == null) {
                Toast.makeText(requireContext(), "Perangkat tidak mendukung Bluetooth", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!adapter.isEnabled) {
                startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                Toast.makeText(requireContext(), "Silakan aktifkan Bluetooth.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Bluetooth sudah aktif.", Toast.LENGTH_SHORT).show()
            }
        }

        // ============= EVENT CUSTOMER PRINTER =============
        binding.btnUbahPrinterCustomer.setOnClickListener {
            if (!checkBtPermissions()) return@setOnClickListener
            showBluetoothDeviceDialog("customer")
        }

        binding.btnHapusPrinterCustomer.setOnClickListener {
            PrinterPrefs.clearCustomerPrinter(requireContext())
            loadPrinters()
        }

        binding.btnTestPrintCustomer.setOnClickListener {
            val mac = PrinterPrefs.getCustomerPrinter(requireContext())
            if (mac.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Belum ada printer customer.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doTestPrint(mac, "Customer")
        }

        // ============= EVENT DAPUR PRINTER =============
        binding.btnUbahPrinterKitchen.setOnClickListener {
            if (!checkBtPermissions()) return@setOnClickListener
            showBluetoothDeviceDialog("dapur")
        }

        binding.btnHapusPrinterKitchen.setOnClickListener {
            PrinterPrefs.clearDapurPrinter(requireContext())
            loadPrinters()
        }

        binding.btnTestPrintKitchen.setOnClickListener {
            val mac = PrinterPrefs.getDapurPrinter(requireContext())
            if (mac.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Belum ada printer dapur.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doTestPrint(mac, "Dapur")
        }

        // Klik item list → pilih printer cepat
        binding.listPairedDevices.setOnItemClickListener { _, _, position, _ ->
            val adapter = binding.listPairedDevices.adapter as? ArrayAdapter<String> ?: return@setOnItemClickListener
            val item = adapter.getItem(position) ?: return@setOnItemClickListener

            val mac = item.substringAfterLast("(").substringBefore(")").trim()

            AlertDialog.Builder(requireContext())
                .setTitle("Set Printer")
                .setMessage("Simpan $item sebagai printer untuk...")
                .setPositiveButton("Customer") { _, _ ->
                    PrinterPrefs.saveCustomerPrinter(requireContext(), mac)
                    loadPrinters()
                }
                .setNegativeButton("Dapur") { _, _ ->
                    PrinterPrefs.saveDapurPrinter(requireContext(), mac)
                    loadPrinters()
                }
                .setNeutralButton("Batal", null)
                .show()
        }
    }

    // ============================
    // LOCKING FUNCTION
    // ============================
    private fun applyPrinterModeLock() {
        val mode = PrinterPrefs.getPrinterMode(requireContext())

        when (mode) {
            "none" -> {
                // Disable semuanya
                setCustomerEnabled(false)
                setKitchenEnabled(false)
            }
            "single" -> {
                // Only customer ON
                setCustomerEnabled(true)
                setKitchenEnabled(false)
            }
            "double" -> {
                // Everything ON
                setCustomerEnabled(true)
                setKitchenEnabled(true)
            }
        }
    }

    private fun setCustomerEnabled(enable: Boolean) {
        binding.btnUbahPrinterCustomer.isEnabled = enable
        binding.btnHapusPrinterCustomer.isEnabled = enable
        binding.btnTestPrintCustomer.isEnabled = enable
    }

    private fun setKitchenEnabled(enable: Boolean) {
        binding.btnUbahPrinterKitchen.isEnabled = enable
        binding.btnHapusPrinterKitchen.isEnabled = enable
        binding.btnTestPrintKitchen.isEnabled = enable
    }

    // ============================
    // BLUETOOTH UTILITY
    // ============================

    private fun updateBluetoothStatus() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            binding.tvStatusBluetooth.text = "Status Bluetooth: Tidak tersedia"
            binding.btnToggleBluetooth.isEnabled = false
            return
        }

        binding.tvStatusBluetooth.text =
            if (adapter.isEnabled) "Status Bluetooth: Aktif"
            else "Status Bluetooth: Mati"
    }

    private fun loadPrinters() {
        val ctx = requireContext()
        binding.tvPrinterCustomer.text = PrinterPrefs.getCustomerPrinter(ctx) ?: "Belum dipilih"
        binding.tvPrinterKitchen.text = PrinterPrefs.getDapurPrinter(ctx) ?: "Belum dipilih"
    }

    @SuppressLint("MissingPermission")
    private fun loadPairedDevicesToList() {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: run {
            binding.listPairedDevices.adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOf("Bluetooth tidak tersedia"))
            return
        }

        if (!adapter.isEnabled) {
            binding.listPairedDevices.adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOf("Nyalakan Bluetooth untuk melihat perangkat"))
            return
        }

        val paired = adapter.bondedDevices
        if (paired.isEmpty()) {
            binding.listPairedDevices.adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOf("Tidak ada perangkat terpasang"))
            return
        }

        val names = paired.map { "${it.name} (${it.address})" }
        binding.listPairedDevices.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)
    }

    private fun checkBtPermissions(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true

        val granted = btPermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

        if (!granted) {
            requestPermissions(btPermissions, REQ_PERM_BT)
        }

        return granted
    }

    @SuppressLint("MissingPermission")
    private fun showBluetoothDeviceDialog(type: String) {
        val ctx = requireContext()
        val adapter = BluetoothAdapter.getDefaultAdapter()

        if (adapter == null) {
            Toast.makeText(ctx, "Device tidak mendukung Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        if (!adapter.isEnabled) {
            Toast.makeText(ctx, "Nyalakan Bluetooth terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val devices = adapter.bondedDevices
        if (devices.isEmpty()) {
            Toast.makeText(ctx, "Tidak ada perangkat yang sudah dipair.", Toast.LENGTH_SHORT).show()
            return
        }

        val list = devices.toList()
        val names = list.map { "${it.name} (${it.address})" }.toTypedArray()

        AlertDialog.Builder(ctx)
            .setTitle(if (type == "customer") "Pilih Printer Customer" else "Pilih Printer Dapur")
            .setItems(names) { _, which ->
                val dev = list[which]
                if (type == "customer") PrinterPrefs.saveCustomerPrinter(ctx, dev.address)
                else PrinterPrefs.saveDapurPrinter(ctx, dev.address)

                loadPrinters()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun doTestPrint(mac: String, label: String) {
        val appCtx = requireContext().applicationContext
        Toast.makeText(requireContext(), "Menghubungkan ke printer $label...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                val socket = BluetoothPrinterManager.connect(mac)
                if (socket == null) return@withContext false
                val bytes = ReceiptFormatter.buildTestReceipt(appCtx)
                val printed = BluetoothPrinterManager.print(socket, bytes)
                BluetoothPrinterManager.close(socket)

                Thread.sleep(300) // <--- tambahin ini
                printed
            }

            Toast.makeText(
                appCtx,
                if (ok) "Test print $label sukses" else "Test print $label gagal",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateBluetoothStatus()
        loadPairedDevicesToList()
        applyPrinterModeLock()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

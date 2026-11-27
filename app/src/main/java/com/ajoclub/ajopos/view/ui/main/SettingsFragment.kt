package com.ajoclub.ajopos.view.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ajoclub.ajopos.R
import com.ajoclub.ajopos.databinding.FragmentSettingsBinding
import com.ajoclub.ajopos.model.database.AppDatabase
import com.ajoclub.ajopos.model.database.DatabaseSeeder
import com.ajoclub.ajopos.util.PrinterPrefs
import com.ajoclub.ajopos.util.ThemePrefs
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()

        // Load mode printer
        when (PrinterPrefs.getPrinterMode(ctx)) {
            "none" -> binding.rbPrinterNone.isChecked = true
            "single" -> binding.rbPrinterSingle.isChecked = true
            "double" -> binding.rbPrinterDouble.isChecked = true
        }

        // Simpan mode printer
        binding.rgPrinterMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbPrinterNone ->
                    PrinterPrefs.savePrinterMode(ctx, "none")

                R.id.rbPrinterSingle ->
                    PrinterPrefs.savePrinterMode(ctx, "single")

                R.id.rbPrinterDouble ->
                    PrinterPrefs.savePrinterMode(ctx, "double")
            }
        }

        // Load mode tema
        when (ThemePrefs.getThemeMode(ctx)) {
            ThemePrefs.MODE_LIGHT -> binding.rbThemeLight.isChecked = true
            ThemePrefs.MODE_DARK -> binding.rbThemeDark.isChecked = true
            else -> binding.rbThemeSystem.isChecked = true
        }

        // Simpan mode tema
        binding.rgThemeMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rbThemeLight -> ThemePrefs.MODE_LIGHT
                R.id.rbThemeDark -> ThemePrefs.MODE_DARK
                else -> ThemePrefs.MODE_SYSTEM
            }
            if (mode != ThemePrefs.getThemeMode(ctx)) {
                ThemePrefs.saveThemeMode(ctx, mode)
            }
        }

        // Navigasi
        binding.btnOpenDeviceManager.setOnClickListener {
            findNavController().navigate(
                R.id.action_settingsFragment_to_deviceManagerFragment
            )
        }

        binding.btnOpenPrinterPref.setOnClickListener {
            findNavController().navigate(
                R.id.action_settingsFragment_to_printerPrefFragment
            )
        }

        binding.btnOpenQris.setOnClickListener {
            findNavController().navigate(
                R.id.qrisSettingsFragment
            )
        }

        binding.btnexportimport.setOnClickListener {
            findNavController().navigate(
                R.id.action_settingsFragment_to_backupRestoreFragment
            )
        }

        // Trigger Seeder Dummy Data
        binding.btnSeedDummy.setOnClickListener {
            AlertDialog.Builder(ctx)
                .setTitle("Isi Data Contoh?")
                .setMessage("Aplikasi akan menambahkan 3 kategori dan 10 produk contoh untuk simulasi transaksi. Data yang sudah ada tidak akan terhapus.")
                .setPositiveButton("Ya, Isi Data") { _, _ ->
                    binding.btnSeedDummy.isEnabled = false
                    viewLifecycleOwner.lifecycleScope.launch {
                        val db = AppDatabase.getDatabase(ctx)
                        val result = DatabaseSeeder.seedDummyData(db)

                        result.onSuccess { msg ->
                            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                        }.onFailure { err ->
                            Toast.makeText(ctx, "Gagal mengisi data: ${err.message}", Toast.LENGTH_SHORT).show()
                        }
                        binding.btnSeedDummy.isEnabled = true
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
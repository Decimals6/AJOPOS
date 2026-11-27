package com.ajoclub.ajopos.view.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.ajoclub.ajopos.databinding.FragmentCreateNotaDialogBinding
import com.ajoclub.ajopos.model.data.CartItem
import com.ajoclub.ajopos.model.data.DetailNota
import com.ajoclub.ajopos.model.data.Nota
import com.ajoclub.ajopos.util.QrisHelper
import com.ajoclub.ajopos.view.ui.pos.QrisPaymentDialog
import com.ajoclub.ajopos.viewmodel.TransaksiViewModel
import com.ajoclub.ajopos.worker.PrintJobWorker
import java.text.NumberFormat
import java.util.*

class CreateNotaDialog(
    private val total: Double,
    private val cartItems: List<CartItem>,
    private val userId: Int = 0,
    private val onCreated: (String) -> Unit
) : DialogFragment() {

    private var _binding: FragmentCreateNotaDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var transaksiViewModel: TransaksiViewModel

    private val TAG = "CreateNotaDialog"

    private fun generateLocalId(): String {
        val sdf = java.text.SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale("in", "ID"))
        val ts = sdf.format(Date())
        return "nota-$ts"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateNotaDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transaksiViewModel = ViewModelProvider(requireActivity())[TransaksiViewModel::class.java]

        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        fmt.maximumFractionDigits = 0
        binding.tvTotal.text = "Total: ${fmt.format(total)}"

        // Observe sukses buat nota
        transaksiViewModel.createdLocalId.observe(viewLifecycleOwner) { event ->
            val createdId = event?.getContentIfNotHandled()
            if (!createdId.isNullOrEmpty()) {
                PrintJobWorker.enqueueNow(requireContext().applicationContext)
                onCreated(createdId)
                binding.btnCreate.isEnabled = true
                dismiss()
            }
        }

        // Observe error
        transaksiViewModel.error.observe(viewLifecycleOwner) { event ->
            val err = event?.getContentIfNotHandled()
            if (!err.isNullOrEmpty()) {
                Log.e(TAG, "Gagal membuat nota di DB: $err")
                Toast.makeText(requireContext(), "Gagal membuat nota: $err", Toast.LENGTH_LONG).show()
                binding.btnCreate.isEnabled = true
            }
        }

        binding.rgPembayaran.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.rbCash.id) {
                binding.etCash.visibility = View.VISIBLE
            } else {
                binding.etCash.visibility = View.GONE
                binding.etCash.text = null
            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnCreate.setOnClickListener {
            handleProsesTransaksi()
        }
    }

    private fun handleProsesTransaksi() {
        val pelanggan = binding.etPelanggan.text?.toString()?.trim()
        if (pelanggan.isNullOrEmpty()) {
            binding.etPelanggan.error = "Nama pelanggan wajib"
            return
        }

        val metodePembayaran = when (binding.rgPembayaran.checkedRadioButtonId) {
            binding.rbCash.id -> "CASH"
            binding.rbQris.id -> "QRIS"
            else -> "CASH"
        }

        val tipe = when (binding.rgTipe.checkedRadioButtonId) {
            binding.rbDineIn.id -> "DINE_IN"
            binding.rbTakeaway.id -> "TAKEAWAY"
            else -> "TAKEAWAY"
        }

        if (metodePembayaran == "CASH") {
            val c = binding.etCash.text?.toString()?.trim()
            if (c.isNullOrEmpty()) {
                binding.etCash.error = "Nominal wajib untuk CASH"
                return
            }
            val nominalCash = c.toDoubleOrNull() ?: 0.0
            if (nominalCash < total) {
                binding.etCash.error = "Uang tunai kurang dari total belanja"
                return
            }
            eksekusiSimpanNota(pelanggan, tipe, "CASH", c)
        } else {
            // Cek apakah string QRIS statis sudah disimpan di pengaturan
            val staticQris = QrisHelper.getStaticQris(requireContext())
            if (staticQris.isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "QRIS toko belum disetel! Harap scan QRIS statis di Pengaturan.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            // Tampilkan popup QRIS dinamis ber-nominal
            val qrisDialog = QrisPaymentDialog.newInstance(total) {
                eksekusiSimpanNota(pelanggan, tipe, "QRIS", null)
            }
            qrisDialog.show(childFragmentManager, "QRIS_PAYMENT_POPUP")
        }
    }

    private fun eksekusiSimpanNota(
        pelanggan: String,
        tipe: String,
        metodePembayaran: String,
        cash: String?
    ) {
        binding.btnCreate.isEnabled = false

        val localId = generateLocalId()
        val now = System.currentTimeMillis()

        val nota = Nota(
            localId = localId,
            serverId = null,
            userId = userId,
            pelanggan = pelanggan,
            waktu = now,
            tipe = tipe,
            total = total,
            pembayaran = metodePembayaran,
            sync = 0,
            created_at = now,
            updated_at = now,
            deleted = 0
        )

        val details = cartItems.map { c ->
            DetailNota(
                notaLocalId = localId,
                produkId = c.produk.produk.id,
                keterangan = c.note ?: "",
                harga = c.produk.produk.harga,
                jumlah = c.quantity,
                subtotal = c.produk.produk.harga * c.quantity,
                sync = 0
            )
        }

        transaksiViewModel.createNotaWithDetailsAndPrintJobs(nota, details, cash)
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
package com.ajoclub.ajopos.view.ui.pos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.ajoclub.ajopos.databinding.DialogQrisPaymentBinding
import com.ajoclub.ajopos.util.QrisHelper
import java.text.NumberFormat
import java.util.Locale

class QrisPaymentDialog : DialogFragment() {

    private var _binding: DialogQrisPaymentBinding? = null
    private val binding get() = _binding!!

    private var totalAmount: Long = 0L
    private var onPaymentSuccess: (() -> Unit)? = null

    companion object {
        fun newInstance(total: Double, onPaymentConfirmed: () -> Unit): QrisPaymentDialog {
            val dialog = QrisPaymentDialog()
            dialog.totalAmount = total.toLong()
            dialog.onPaymentSuccess = onPaymentConfirmed
            return dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = DialogQrisPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        binding.tvNominalQris.text = rupiah.format(totalAmount)

        val rawQris = QrisHelper.getStaticQris(requireContext())
        if (rawQris.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "QRIS Toko belum disetel di pengaturan!", Toast.LENGTH_LONG).show()
            dismiss()
            return
        }

        try {
            // Generate QRIS Dinamis ber-nominal
            val dynamicPayload = QrisHelper.createDynamicQris(rawQris, totalAmount)
            val bmp = QrisHelper.generateQrBitmap(dynamicPayload, 500)
            binding.ivDynamicQris.setImageBitmap(bmp)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Gagal generate QRIS Dinamis", Toast.LENGTH_SHORT).show()
        }

        binding.btnBatalQris.setOnClickListener { dismiss() }

        binding.btnSelesaiBayar.setOnClickListener {
            onPaymentSuccess?.invoke()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
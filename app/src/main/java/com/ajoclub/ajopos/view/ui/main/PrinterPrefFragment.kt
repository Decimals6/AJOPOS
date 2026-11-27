package com.ajoclub.ajopos.view.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ajoclub.ajopos.R
import com.ajoclub.ajopos.bt.ReceiptFormatter
import com.ajoclub.ajopos.util.PrinterPrefs

class PrinterPrefFragment : Fragment() {

    private lateinit var etStoreName: EditText
    private lateinit var etStoreAddress: EditText
    private lateinit var etStorePhone: EditText

    private lateinit var rbPaper58: RadioButton
    private lateinit var rbPaper58On80: RadioButton
    private lateinit var rbPaper80: RadioButton

    private lateinit var btnSave: Button
    private lateinit var btnReset: Button
    private lateinit var btnPreview: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_printer_pref,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // =====================
        // INIT VIEW
        // =====================
        etStoreName = view.findViewById(R.id.etStoreName)
        etStoreAddress = view.findViewById(R.id.etStoreAddress)
        etStorePhone = view.findViewById(R.id.etStorePhone)

        rbPaper58 = view.findViewById(R.id.rbPaper58)
        rbPaper58On80 = view.findViewById(R.id.rbPaper58On80)
        rbPaper80 = view.findViewById(R.id.rbPaper80)

        btnSave = view.findViewById(R.id.btnSavePrinterPref)
        btnReset = view.findViewById(R.id.btnResetPrinterPref)
        btnPreview = view.findViewById(R.id.btnPreviewReceipt)

        // =====================
        // LOAD SAVED PREFERENCES
        // =====================
        loadPreferences()

        // =====================
        // SAVE
        // =====================
        btnSave.setOnClickListener {
            savePreferences()
        }

        // =====================
        // RESET
        // =====================
        btnReset.setOnClickListener {
            resetPreferences()
        }

        btnPreview.setOnClickListener {
            showReceiptPreviewDialog()
        }
    }

    private fun getSelectedPaperMode(): Int {
        return when {
            rbPaper80.isChecked -> PrinterPrefs.PAPER_80_NATIVE
            rbPaper58On80.isChecked -> PrinterPrefs.PAPER_58_ON_80
            else -> PrinterPrefs.PAPER_58_NATIVE
        }
    }

    private fun loadPreferences() {
        val context = requireContext()

        etStoreName.setText(PrinterPrefs.getStoreName(context))
        etStoreAddress.setText(PrinterPrefs.getStoreAddress(context))
        etStorePhone.setText(PrinterPrefs.getStorePhone(context))

        when (PrinterPrefs.getPaperWidth(context)) {
            PrinterPrefs.PAPER_80_NATIVE -> rbPaper80.isChecked = true
            PrinterPrefs.PAPER_58_ON_80 -> rbPaper58On80.isChecked = true
            else -> rbPaper58.isChecked = true
        }
    }

    private fun savePreferences() {
        val context = requireContext()

        val storeName = etStoreName.text.toString().trim()
        val storeAddress = etStoreAddress.text.toString().trim()
        val storePhone = etStorePhone.text.toString().trim()

        if (storeName.isBlank()) {
            etStoreName.error = "Nama toko tidak boleh kosong"
            etStoreName.requestFocus()
            return
        }

        val paperMode = getSelectedPaperMode()

        PrinterPrefs.saveStoreName(context, storeName)
        PrinterPrefs.saveStoreAddress(context, storeAddress)
        PrinterPrefs.saveStorePhone(context, storePhone)
        PrinterPrefs.savePaperWidth(context, paperMode)

        Toast.makeText(
            context,
            "Pengaturan printer berhasil disimpan",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun resetPreferences() {
        val context = requireContext()

        PrinterPrefs.saveStoreName(context, "AJOCLUB")
        PrinterPrefs.saveStoreAddress(
            context,
            "Jl. Intan 3 no 42\nKec. Paras, Mulung"
        )
        PrinterPrefs.saveStorePhone(context, "+12345678910")
        PrinterPrefs.savePaperWidth(context, PrinterPrefs.PAPER_58_NATIVE)

        loadPreferences()

        Toast.makeText(
            context,
            "Pengaturan dikembalikan ke default",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showReceiptPreviewDialog() {
        val context = requireContext()

        val selectedMode = getSelectedPaperMode()
        val currentName = etStoreName.text.toString().trim()
        val currentAddress = etStoreAddress.text.toString().trim()
        val currentPhone = etStorePhone.text.toString().trim()

        val (dummyNota, dummyItems) = ReceiptFormatter.generateDummyNota()
        val receiptText = ReceiptFormatter.buildReceiptPlainText(
            ctx = context,
            nota = dummyNota,
            items = dummyItems,
            targetPaperWidth = selectedMode,
            customStoreName = currentName,
            customStoreAddress = currentAddress,
            customStorePhone = currentPhone
        )

        val is80mm = selectedMode == PrinterPrefs.PAPER_80_NATIVE
        val dynamicTextSize = if (is80mm) 9.5f else 11.5f
        val titleLabel = when (selectedMode) {
            PrinterPrefs.PAPER_80_NATIVE -> "80mm"
            PrinterPrefs.PAPER_58_ON_80 -> "58mm - Sekat 80mm"
            else -> "58mm"
        }

        val textView = android.widget.TextView(context).apply {
            text = receiptText
            typeface = android.graphics.Typeface.MONOSPACE
            textSize = dynamicTextSize
            setTextColor(android.graphics.Color.parseColor("#212121"))
            setBackgroundColor(android.graphics.Color.WHITE)
            setPadding(32, 32, 32, 32)
            setHorizontallyScrolling(true)
        }

        val cardPaper = com.google.android.material.card.MaterialCardView(context).apply {
            radius = 8f
            cardElevation = 6f
            setCardBackgroundColor(android.graphics.Color.WHITE)
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                topMargin = 16
                bottomMargin = 16
            }
            addView(textView)
        }

        val centerContainer = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(android.graphics.Color.parseColor("#F1F5F9"))
            addView(cardPaper)
        }

        val scrollView = android.widget.ScrollView(context).apply {
            addView(centerContainer)
        }

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Preview Nota ($titleLabel)")
            .setView(scrollView)
            .setPositiveButton("Tutup", null)
            .show()
    }
}
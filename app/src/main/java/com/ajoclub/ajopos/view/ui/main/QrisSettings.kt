package com.ajoclub.ajopos.view.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ajoclub.ajopos.databinding.FragmentQrisSettingsBinding
import com.ajoclub.ajopos.util.PortraitCaptureActivity
import com.ajoclub.ajopos.util.QrisHelper
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class QrisSettingsFragment : Fragment() {

    private var _binding: FragmentQrisSettingsBinding? = null
    private val binding get() = _binding!!

    private val scanQrisLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val qrisText = result.contents
            binding.etQrisString.setText(qrisText)
            saveAndShowPreview(qrisText)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrisSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadExistingQris()

        binding.btnScanQris.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Arahkan kamera ke QRIS fisik toko Anda")
                setBeepEnabled(true)
                setOrientationLocked(true)
                setCaptureActivity(PortraitCaptureActivity::class.java)
            }
            scanQrisLauncher.launch(options)
        }

        binding.btnSaveQris.setOnClickListener {
            val qris = binding.etQrisString.text.toString().trim()
            if (qris.isEmpty()) {
                Toast.makeText(requireContext(), "Kode QRIS tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveAndShowPreview(qris)
        }
    }

    private fun loadExistingQris() {
        val saved = QrisHelper.getStaticQris(requireContext())
        if (!saved.isNullOrEmpty()) {
            binding.etQrisString.setText(saved)
            displayQrPreview(saved)
        }
    }

    private fun saveAndShowPreview(rawQris: String) {
        QrisHelper.saveStaticQris(requireContext(), rawQris)
        displayQrPreview(rawQris)
        Toast.makeText(requireContext(), "QRIS Toko berhasil disimpan!", Toast.LENGTH_SHORT).show()
    }

    private fun displayQrPreview(rawQris: String) {
        try {
            val bmp = QrisHelper.generateQrBitmap(rawQris, 400)
            binding.ivQrisPreview.setImageBitmap(bmp)
            binding.ivQrisPreview.imageTintList = null
            binding.tvStatusQris.text = "QRIS Aktif Siap Digunakan"
        } catch (e: Exception) {
            binding.tvStatusQris.text = "Format QRIS tidak valid"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
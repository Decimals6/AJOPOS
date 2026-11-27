package com.ajoclub.ajopos.view.ui.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ajoclub.ajopos.databinding.DialogContinousScannerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult

class ContinuousScanDialog(
    private val onBarcodeScanned: (barcode: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogContinousScannerBinding? = null
    private val binding get() = _binding!!

    private lateinit var beepManager: BeepManager
    private var lastScannedCode = ""
    private var lastScannedTimestamp = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogContinousScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        beepManager = BeepManager(requireActivity())
        beepManager.isBeepEnabled = true

        val callback = object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                val code = result?.text ?: return
                val now = System.currentTimeMillis()

                // Debounce 1.2 detik untuk barcode yang sama agar tidak spam
                if (code == lastScannedCode && (now - lastScannedTimestamp) < 1200) {
                    return
                }

                lastScannedCode = code
                lastScannedTimestamp = now

                beepManager.playBeepSoundAndVibrate()

                binding.tvScanStatus.text = "Terdeteksi: $code"
                onBarcodeScanned(code)

                // Kembalikan status teks setelah 1 detik
                Handler(Looper.getMainLooper()).postDelayed({
                    if (_binding != null) {
                        binding.tvScanStatus.text = "Siap scan barang berikutnya..."
                    }
                }, 1000)
            }
        }

        binding.barcodeScannerView.decodeContinuous(callback)

        binding.btnCloseScanner.setOnClickListener { dismiss() }
        binding.btnSelesaiScan.setOnClickListener { dismiss() }
    }

    override fun onResume() {
        super.onResume()
        binding.barcodeScannerView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeScannerView.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
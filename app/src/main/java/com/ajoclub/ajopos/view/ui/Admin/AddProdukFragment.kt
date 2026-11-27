package com.ajoclub.ajopos.view.ui.Admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.ajoclub.ajopos.databinding.FragmentAddEditProdukBinding
import com.ajoclub.ajopos.model.data.Kategori
import com.ajoclub.ajopos.model.data.Produk
import com.ajoclub.ajopos.util.ImageStorageHelper
import com.ajoclub.ajopos.util.PortraitCaptureActivity
import com.ajoclub.ajopos.viewmodel.KategoriViewModel
import com.ajoclub.ajopos.viewmodel.ProdukViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class AddProdukFragment : Fragment() {
    private lateinit var binding: FragmentAddEditProdukBinding
    private lateinit var produkViewModel: ProdukViewModel
    private lateinit var kategoriViewModel: KategoriViewModel

    private var selectedKategori: Kategori? = null
    private var kategoriList: List<Kategori> = listOf()
    private var savedImageFileName: String? = null

    // Register Photo Picker
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val fileName = ImageStorageHelper.saveImageToInternal(requireContext(), uri)
            if (fileName != null) {
                savedImageFileName = fileName
                val file = ImageStorageHelper.getImageFile(requireContext(), fileName)
                Glide.with(this).load(file).into(binding.ivPreviewProduk)
            }
        }
    }

    // Register Barcode Scanner
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            binding.etBarcode.setText(result.contents)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddEditProdukBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        produkViewModel = ViewModelProvider(this)[ProdukViewModel::class.java]
        kategoriViewModel = ViewModelProvider(this)[KategoriViewModel::class.java]

        setupSpinner()
        setupListeners()

        kategoriViewModel.getAllKategori()
        observeKategoriList()
    }

    private fun setupSpinner() {
        binding.spinnerKategori.setOnItemClickListener { _, _, position, _ ->
            selectedKategori = kategoriList.getOrNull(position)
        }
    }

    private fun observeKategoriList() {
        kategoriViewModel.kategoriLD.observe(viewLifecycleOwner) { list ->
            kategoriList = list.filter { it.deleted == 0 }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                kategoriList.map { it.nama.replaceFirstChar { char -> char.uppercase() } }
            )
            binding.spinnerKategori.setAdapter(adapter)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnPilihGambar.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnScanBarcodeCard.setOnClickListener {
            binding.btnScanBarcode.performClick()
        }

        binding.btnScanBarcode.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Arahkan kamera ke barcode produk")
                setBeepEnabled(true)
                setOrientationLocked(true)
                setCaptureActivity(PortraitCaptureActivity::class.java)
            }
            barcodeLauncher.launch(options)
        }

        binding.btnSimpanProduk.setOnClickListener {
            val nama = binding.etNamaProduk.text.toString().trim()
            val hargaStr = binding.etHargaProduk.text.toString().trim()
            val keterangan = binding.etKeteranganProduk.text.toString().trim()
            val barcode = binding.etBarcode.text.toString().trim().ifEmpty { null }

            if (nama.isEmpty()) {
                Toast.makeText(requireContext(), "Nama produk tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val harga = hargaStr.toDoubleOrNull()
            if (harga == null || harga <= 0) {
                Toast.makeText(requireContext(), "Harga tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val kategori = selectedKategori
            if (kategori == null) {
                Toast.makeText(requireContext(), "Pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentTime = System.currentTimeMillis()

            val produk = Produk(
                id = 0,
                nama = nama,
                barcode = barcode,
                gambar = savedImageFileName,
                harga = harga,
                keterangan = keterangan,
                kategoriId = kategori.id,
                deleted = 0,
                created_at = currentTime,
                updated_at = currentTime
            )

            produkViewModel.addProduk(produk)
            Toast.makeText(requireContext(), "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}

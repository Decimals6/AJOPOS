package com.ajoclub.ajopos.view.ui.Admin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
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

class EditProdukFragment : Fragment() {
    private lateinit var binding: FragmentAddEditProdukBinding
    private lateinit var produkViewModel: ProdukViewModel
    private lateinit var kategoriViewModel: KategoriViewModel

    private var selectedKategori: Kategori? = null
    private var kategoriList: List<Kategori> = listOf()
    private var currentProduk: Produk? = null
    private var updatedImageFileName: String? = null

    private var pendingCameraAction: (() -> Unit)? = null

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pendingCameraAction?.invoke()
            } else {
                Toast.makeText(requireContext(), "Izin kamera diperlukan untuk memindai barcode.", Toast.LENGTH_SHORT).show()
            }
            pendingCameraAction = null
        }

    private fun checkCameraPermission(onGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            pendingCameraAction = onGranted
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val fileName = ImageStorageHelper.saveImageToInternal(requireContext(), uri)
            if (fileName != null) {
                updatedImageFileName = fileName
                val file = ImageStorageHelper.getImageFile(requireContext(), fileName)
                Glide.with(this).load(file).into(binding.ivPreviewProduk)
            }
        }
    }

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

        binding.tvFormTitle.text = "Edit Produk"
        binding.btnSimpanProduk.text = "Update Produk"

        setupSpinner()
        setupListeners()

        val produkId = EditProdukFragmentArgs.fromBundle(requireArguments()).id

        kategoriViewModel.getAllKategori()
        observeKategoriList()

        produkViewModel.getProdukById(produkId)
        observeProduk()
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

            currentProduk?.let { produk ->
                val index = kategoriList.indexOfFirst { it.id == produk.kategoriId }
                if (index >= 0) {
                    val kat = kategoriList[index]
                    binding.spinnerKategori.setText(kat.nama.replaceFirstChar { char -> char.uppercase() }, false)
                    selectedKategori = kat
                }
            }
        }
    }

    private fun observeProduk() {
        produkViewModel.selectedProdukLD.observe(viewLifecycleOwner) { pwk ->
            pwk?.let {
                val produk = it.produk
                currentProduk = produk
                updatedImageFileName = produk.gambar

                binding.etNamaProduk.setText(produk.nama)
                binding.etBarcode.setText(produk.barcode ?: "")
                binding.etHargaProduk.setText(produk.harga.toLong().toString())
                binding.etKeteranganProduk.setText(produk.keterangan)

                // Load gambar existing
                val imgFile = ImageStorageHelper.getImageFile(requireContext(), produk.gambar)
                if (imgFile != null) {
                    Glide.with(this).load(imgFile).into(binding.ivPreviewProduk)
                }

                val index = kategoriList.indexOfFirst { k -> k.id == produk.kategoriId }
                if (index >= 0) {
                    val kat = kategoriList[index]
                    binding.spinnerKategori.setText(kat.nama.replaceFirstChar { char -> char.uppercase() }, false)
                    selectedKategori = kat
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        binding.btnPilihGambar.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnScanBarcodeCard.setOnClickListener {
            binding.btnScanBarcode.performClick()
        }

        binding.btnScanBarcode.setOnClickListener {
            checkCameraPermission {
                val options = ScanOptions().apply {
                    setPrompt("Arahkan kamera ke barcode produk")
                    setBeepEnabled(true)
                    setOrientationLocked(true)
                    setCaptureActivity(PortraitCaptureActivity::class.java)
                }
                barcodeLauncher.launch(options)
            }
        }

        binding.btnSimpanProduk.setOnClickListener { view ->
            val nama = binding.etNamaProduk.text.toString().trim()
            val hargaStr = binding.etHargaProduk.text.toString().trim()
            val keterangan = binding.etKeteranganProduk.text.toString().trim()
            val barcode = binding.etBarcode.text.toString().trim().ifEmpty { null }
            val kategori = selectedKategori
            val produkLama = currentProduk

            if (nama.isEmpty() || hargaStr.isEmpty() || kategori == null || produkLama == null) {
                Toast.makeText(requireContext(), "Lengkapi semua field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val harga = hargaStr.toDoubleOrNull() ?: 0.0

            val updatedProduk = produkLama.copy(
                nama = nama,
                barcode = barcode,
                gambar = updatedImageFileName,
                harga = harga,
                keterangan = keterangan,
                kategoriId = kategori.id,
                updated_at = System.currentTimeMillis()
            )

            produkViewModel.updateProduk(updatedProduk)
            Toast.makeText(requireContext(), "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(view).popBackStack()
        }
    }
}

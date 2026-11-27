package com.ajoclub.ajopos.view.ui.main

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajoclub.ajopos.databinding.FragmentHomeBinding
import com.ajoclub.ajopos.model.data.CartItem
import com.ajoclub.ajopos.model.data.ProdukWithKategori
import com.ajoclub.ajopos.view.adapter.main.CartAdapter
import com.ajoclub.ajopos.view.adapter.main.KategoriChipAdapter
import com.ajoclub.ajopos.view.adapter.main.PosProdukAdapter
import com.ajoclub.ajopos.viewmodel.ProdukViewModel
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: ProdukViewModel
    private lateinit var kategoriAdapter: KategoriChipAdapter
    private lateinit var posProdukAdapter: PosProdukAdapter
    private lateinit var cartAdapter: CartAdapter
    private val cartList = mutableListOf<CartItem>()
    private val currentUserId: Int = 0
    private var cartBottomSheetDialog: CartBottomSheetDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ProdukViewModel::class.java]

        val spanCount = getDynamicSpanCount()

        // 1. Adapter produk katalog
        posProdukAdapter = PosProdukAdapter(arrayListOf()) { produk ->
            addToCart(produk)
        }

        binding.rvProduk.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = posProdukAdapter
        }

        // 2. Adapter Cart
        cartAdapter = CartAdapter(cartList) { updatedCart ->
            updateCartTotal(updatedCart)
        }

        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }

        // 3. Tombol Bayar
        binding.btnBayar.setOnClickListener {
            showCreateNotaDialog()
        }

        // 4. Pencarian teks / filter
        binding.etSearch.addTextChangedListener { editable ->
            val query = editable?.toString() ?: ""
            viewModel.filterProduk(query)
        }

        // 5. Kategori Chip
        kategoriAdapter = KategoriChipAdapter(arrayListOf()) { kategori ->
            if (kategori == null) {
                viewModel.filterByKategori(null)
            } else {
                viewModel.filterByKategori(kategori.id)
            }
        }

        binding.rvKategori.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = kategoriAdapter
        }

        // 6. Tombol Scan Barcode Kasir Berkelanjutan (Mode Indomaret)
        binding.btnContinuousScan.setOnClickListener {
            val scannerDialog = ContinuousScanDialog { barcode ->
                viewModel.findByBarcode(barcode)
            }
            scannerDialog.show(childFragmentManager, "CONTINUOUS_SCANNER")
        }

        // 7. Tombol Reset / Clear Keranjang
        binding.btnResetCart.setOnClickListener {
            if (cartList.isNotEmpty()) {
                cartAdapter.clearAll()
                updateCartTotal(emptyList())
            }
        }

        // 8. Floating Cart Bar click listener -> Open BottomSheet
        binding.cardFloatingCart?.setOnClickListener {
            if (cartList.isNotEmpty()) {
                showCartBottomSheet()
            }
        }

        // Observe produk dari ViewModel
        viewModel.getAllProduk()
        viewModel.getAllKategori()

        observeViewModel()
    }

    private fun showCartBottomSheet() {
        cartBottomSheetDialog = CartBottomSheetDialog(
            cartList = cartList,
            cartAdapter = cartAdapter,
            onCheckout = {
                showCreateNotaDialog()
            },
            onCartUpdated = { updatedCart ->
                updateCartTotal(updatedCart)
            }
        )
        cartBottomSheetDialog?.show(childFragmentManager, "CART_BOTTOM_SHEET")
    }

    private fun addToCart(produk: ProdukWithKategori) {
        val existingItem = cartList.find {
            it.produk.produk.id == produk.produk.id
        }

        if (existingItem == null) {
            cartList.add(CartItem(produk))
        } else {
            existingItem.quantity++
        }

        cartAdapter.notifyDataSetChanged()
        updateCartTotal(cartList)
    }

    private fun showCreateNotaDialog() {
        val items = cartAdapter.getItems()
        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "Keranjang kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val total = items.sumOf { it.produk.produk.harga * it.quantity }

        val dialog = CreateNotaDialog(
            total = total,
            cartItems = items,
            userId = currentUserId,
            onCreated = { localId ->
                Toast.makeText(
                    requireContext(),
                    "Nota dibuat: $localId",
                    Toast.LENGTH_SHORT
                ).show()

                cartAdapter.clearAll()
                updateCartTotal(emptyList())
            }
        )
        dialog.show(childFragmentManager, "create_nota")
    }

    private fun observeViewModel() {
        viewModel.produkLD.observe(viewLifecycleOwner, Observer { list ->
            posProdukAdapter.updateProdukList(list)
        })
        viewModel.kategoriLD.observe(viewLifecycleOwner) { list ->
            kategoriAdapter.updateKategori(list)
        }

        // Observer hasil scan barcode
        viewModel.scannedProdukLD.observe(viewLifecycleOwner) { produkScanned ->
            if (produkScanned != null) {
                addToCart(produkScanned)
                Toast.makeText(requireContext(), "${produkScanned.produk.nama} (+1)", Toast.LENGTH_SHORT).show()
                viewModel.scannedProdukLD.value = null // reset trigger
            } else {
                Toast.makeText(requireContext(), "Barang tidak terdaftar di sistem!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDynamicSpanCount(): Int {
        val config = resources.configuration
        val swDp = config.smallestScreenWidthDp
        val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE

        return when {
            swDp >= 720 -> if (isLandscape) 6 else 5
            swDp >= 600 -> if (isLandscape) 5 else 4
            else -> if (isLandscape) 2 else 2
        }
    }

    private fun formatRupiah(total: Number): String {
        return "Rp${NumberFormat.getInstance(Locale("in", "ID")).format(total)}"
    }

    private fun updateCartTotal(items: List<CartItem> = cartAdapter.getItems()) {
        val total = items.sumOf {
            it.produk.produk.harga * it.quantity
        }
        val itemCount = items.sumOf { it.quantity }
        binding.tvTotal.text = formatRupiah(total)
        binding.tvCartBadge?.text = "$itemCount item di keranjang"

        // Floating Cart Bar UI
        binding.tvFloatingItemCount?.text = "$itemCount Item"
        binding.tvFloatingTotal?.text = formatRupiah(total)

        if (itemCount > 0) {
            binding.cardFloatingCart?.visibility = View.VISIBLE
        } else {
            binding.cardFloatingCart?.visibility = View.GONE
            cartBottomSheetDialog?.dismiss()
        }

        cartBottomSheetDialog?.updateTotalUI(items)
    }
}

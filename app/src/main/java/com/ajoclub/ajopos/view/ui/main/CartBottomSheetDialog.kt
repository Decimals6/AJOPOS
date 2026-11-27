package com.ajoclub.ajopos.view.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajoclub.ajopos.databinding.DialogCartBottomSheetBinding
import com.ajoclub.ajopos.model.data.CartItem
import com.ajoclub.ajopos.view.adapter.main.CartAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.NumberFormat
import java.util.Locale

class CartBottomSheetDialog(
    private val cartList: MutableList<CartItem>,
    private val cartAdapter: CartAdapter,
    private val onCheckout: () -> Unit,
    private val onCartUpdated: (List<CartItem>) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogCartBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCartBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView Cart
        binding.rvCartSheet.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }

        updateTotalUI(cartList)

        // Tombol Bersihkan Keranjang
        binding.btnResetCartSheet.setOnClickListener {
            if (cartList.isNotEmpty()) {
                cartAdapter.clearAll()
                onCartUpdated(emptyList())
                dismiss()
            }
        }

        // Tombol Lanjut Pembayaran
        binding.btnBayarSheet.setOnClickListener {
            if (cartList.isEmpty()) {
                Toast.makeText(requireContext(), "Keranjang kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dismiss()
            onCheckout()
        }
    }

    fun updateTotalUI(items: List<CartItem>) {
        if (_binding == null) return
        val total = items.sumOf { it.produk.produk.harga * it.quantity }
        val itemCount = items.sumOf { it.quantity }
        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        fmt.maximumFractionDigits = 0

        binding.tvSheetTitle.text = "Keranjang ($itemCount Item)"
        binding.tvTotalSheet.text = fmt.format(total)

        if (items.isEmpty()) {
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

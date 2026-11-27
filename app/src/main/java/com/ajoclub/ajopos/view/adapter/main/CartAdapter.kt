package com.ajoclub.ajopos.view.adapter.main

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ajoclub.ajopos.databinding.ItemNotaEntryBinding
import com.ajoclub.ajopos.model.data.CartItem
import com.ajoclub.ajopos.model.data.ProdukWithKategori
import java.text.NumberFormat
import java.util.*

class CartAdapter(
    private val cartList: MutableList<CartItem>,
    private val onCartChanged: (List<CartItem>) -> Unit // callback ke fragment
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemNotaEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItem) {
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            formatRupiah.maximumFractionDigits = 0

            binding.apply {
                tvCartItemName.text = item.produk.produk.nama
                tvCartItemQty.text = item.quantity.toString()
                tvCartItemSubtotal.text = formatRupiah.format(item.produk.produk.harga * item.quantity)
                etCartItemNote.setText(item.note ?: "")

                // Tombol tambah
                btnPlus.setOnClickListener {
                    item.quantity++
                    notifyItemChanged(adapterPosition)
                    onCartChanged(cartList)
                }

                // Tombol kurang
                btnMinus.setOnClickListener {
                    if (item.quantity > 1) {
                        item.quantity--
                        notifyItemChanged(adapterPosition)
                        onCartChanged(cartList)
                    }
                }

                // Tombol hapus
                btnDelete.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        cartList.removeAt(pos)
                        notifyItemRemoved(pos)
                        onCartChanged(cartList)
                    }
                }

                // Catatan produk
                etCartItemNote.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        item.note = s.toString()
                        onCartChanged(cartList)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotaEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cartList[position])
    }

    override fun getItemCount(): Int = cartList.size

    fun addItem(produk: ProdukWithKategori) {
        val existing = cartList.find { it.produk.produk.id == produk.produk.id }
        if (existing != null) {
            existing.quantity++
        } else {
            cartList.add(CartItem(produk))
        }
        notifyDataSetChanged()
        onCartChanged(cartList)
    }

    fun clear() {
        cartList.clear()
        notifyDataSetChanged()
        onCartChanged(cartList)
    }
    fun getItems(): List<CartItem> = cartList.toList()

    fun clearAll() {
        cartList.clear()
        notifyDataSetChanged()
        onCartChanged(cartList)
    }
}

// Data model tambahan buat cart


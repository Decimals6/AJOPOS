package com.ajoclub.ajopos.view.adapter.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ajoclub.ajopos.R
import com.ajoclub.ajopos.databinding.ItemProductCardBinding
import com.ajoclub.ajopos.model.data.ProdukWithKategori
import com.ajoclub.ajopos.util.ImageStorageHelper
import java.text.NumberFormat
import java.util.Locale

class PosProdukAdapter(
    private var produkList: MutableList<ProdukWithKategori>,
    private var onProductClick: ((ProdukWithKategori) -> Unit)? = null
) : RecyclerView.Adapter<PosProdukAdapter.PosProdukViewHolder>() {

    inner class PosProdukViewHolder(val binding: ItemProductCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosProdukViewHolder {
        val binding = ItemProductCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PosProdukViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PosProdukViewHolder, position: Int) {
        val item = produkList[position]
        val produk = item.produk
        val imgFile = ImageStorageHelper.getImageFile(holder.itemView.context, produk.gambar)
        if (imgFile != null) {
            Glide.with(holder.itemView.context)
                .load(imgFile)
                .placeholder(R.drawable.baseline_inventory_24)
                .into(holder.binding.imgProduct)
        } else {
            holder.binding.imgProduct.setImageResource(R.drawable.baseline_inventory_24)
        }

        // format harga ke format Rupiah tanpa desimal
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0

        holder.binding.apply {
            tvProductName.text = produk.nama
            tvProductPrice.text = formatter.format(produk.harga)

            // kalau nanti produk punya gambar, bisa diset di sini
            // contoh: imgProduct.setImageResource(R.drawable.nama_gambar)
            // atau pakai Glide: Glide.with(imgProduct).load(produk.imageUrl).into(imgProduct)

            root.setOnClickListener {
                onProductClick?.invoke(item)
            }
        }
    }

    override fun getItemCount(): Int = produkList.size

    fun updateProdukList(newList: List<ProdukWithKategori>) {
        produkList = newList.toMutableList()
        notifyDataSetChanged()
    }
    fun setOnProductClick(listener: (ProdukWithKategori) -> Unit) {
        this.onProductClick = listener
    }

}

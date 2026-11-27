package com.ajoclub.ajopos.view.adapter.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ajoclub.ajopos.R
import com.ajoclub.ajopos.databinding.ProdukCardBinding
import com.ajoclub.ajopos.model.data.ProdukWithKategori
import com.ajoclub.ajopos.util.ImageStorageHelper
import com.ajoclub.ajopos.viewmodel.ProdukViewModel
import java.text.NumberFormat
import java.util.Locale

class   ProdukAdapter(
    private var produkList: MutableList<ProdukWithKategori>,
    private val onItemClick: ((ProdukWithKategori) -> Unit)? = null,
    private val viewModel: ProdukViewModel
) : RecyclerView.Adapter<ProdukAdapter.ProdukViewHolder>() {

    class ProdukViewHolder(val binding: ProdukCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val binding = ProdukCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProdukViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        val item = produkList[position]
        val produk = item.produk
        val kategori = item.kategori
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        val imgFile = ImageStorageHelper.getImageFile(holder.itemView.context, produk.gambar)
        if (imgFile != null) {
            Glide.with(holder.itemView.context)
                .load(imgFile)
                .placeholder(R.drawable.baseline_inventory_24)
                .into(holder.binding.ivMasterImg)
        } else {
            holder.binding.ivMasterImg.setImageResource(R.drawable.baseline_inventory_24)
        }

        holder.binding.apply {
            tvNamaProduk.text = produk.nama
            tvKategoriProduk.text = "Kategori: ${kategori.nama}"
            tvHargaProduk.text = formatter.format(produk.harga)

            btnDelete.setOnClickListener {
                val context = holder.itemView.context

                AlertDialog.Builder(context)
                    .setTitle("Hapus Produk")
                    .setMessage("Apakah Anda yakin ingin menghapus produk \"${produk.nama}\"?")
                    .setPositiveButton("Iya") { dialog, _ ->
                        // panggil viewmodel delete
                        viewModel.deleteProduk(produk)

                        // hapus dari list adapter biar langsung hilang dari UI
                        val newList = produkList.toMutableList()
                        newList.removeAt(position)
                        produkList = newList
                        notifyItemRemoved(position)

                        dialog.dismiss()
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }


    }

    override fun getItemCount(): Int = produkList.size

    fun updateProdukList(newList: List<ProdukWithKategori>) {
        produkList.clear()
        produkList.addAll(newList)
        notifyDataSetChanged()
    }
}

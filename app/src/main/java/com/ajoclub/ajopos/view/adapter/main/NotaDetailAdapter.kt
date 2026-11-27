package com.ajoclub.ajopos.view.adapter.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ajoclub.ajopos.databinding.ItemDetailNotaBinding
import com.ajoclub.ajopos.model.data.DetailNotaDisplay
import java.text.NumberFormat
import java.util.*

class NotaDetailAdapter(
    private var list: MutableList<DetailNotaDisplay> = mutableListOf()
) : RecyclerView.Adapter<NotaDetailAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDetailNotaBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DetailNotaDisplay) {
            val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            fmt.maximumFractionDigits = 0

            binding.apply {
                tvProdukNama.text = item.produkNama
                tvHarga.text = "${item.jumlah} × ${fmt.format(item.harga)}"
                tvSubtotal.text = fmt.format(item.subtotal)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemDetailNotaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun submitList(newList: List<DetailNotaDisplay>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
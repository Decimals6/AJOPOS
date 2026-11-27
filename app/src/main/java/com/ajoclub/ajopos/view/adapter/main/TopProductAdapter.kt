package com.ajoclub.ajopos.ui.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ajoclub.ajopos.R
import com.ajoclub.ajopos.model.data.TopProductDisplay
import java.text.NumberFormat
import java.util.Locale

class TopProductAdapter : RecyclerView.Adapter<TopProductAdapter.ViewHolder>() {

    private val items = mutableListOf<TopProductDisplay>()
    private val rupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    fun submitList(newItems: List<TopProductDisplay>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_top_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvItemName)
        private val tvQty: TextView = itemView.findViewById(R.id.tvItemQty)
        private val tvRevenue: TextView = itemView.findViewById(R.id.tvItemRevenue)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        fun bind(item: TopProductDisplay) {
            tvName.text = item.nama
            tvQty.text = "${item.totalQty} terjual"
            tvRevenue.text = "Omzet: ${rupiah.format(item.totalRevenue)}"
            progressBar.progress = item.percentage
        }
    }
}
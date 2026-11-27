package com.ajoclub.ajopos.view.adapter.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ajoclub.ajopos.R
import com.ajoclub.ajopos.model.data.Kategori
import com.google.android.material.card.MaterialCardView

class KategoriChipAdapter(
    private var items: List<Kategori>,
    private val onClick: (Kategori?) -> Unit
) : RecyclerView.Adapter<KategoriChipAdapter.VH>() {

    private var selectedId = 0 // 0 = ALL

    inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view.findViewById(R.id.tvNamaKategori)
        val card: MaterialCardView = view.findViewById(R.id.chipCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chip_kategori, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val context = holder.itemView.context
        val kategori = if (position == 0) null else items[position - 1]
        holder.tv.text = kategori?.nama ?: "Semua"

        val isSelected = (kategori?.id ?: 0) == selectedId
        val primaryColor = ContextCompat.getColor(context, R.color.primary)
        val onPrimaryColor = ContextCompat.getColor(context, R.color.on_primary)
        val surfaceColor = ContextCompat.getColor(context, R.color.surface)
        val onSurfaceColor = ContextCompat.getColor(context, R.color.on_surface)
        val outlineColor = ContextCompat.getColor(context, R.color.outline)

        if (isSelected) {
            holder.card.setCardBackgroundColor(primaryColor)
            holder.tv.setTextColor(onPrimaryColor)
            holder.card.strokeColor = primaryColor
            holder.card.cardElevation = 2f
        } else {
            holder.card.setCardBackgroundColor(surfaceColor)
            holder.tv.setTextColor(onSurfaceColor)
            holder.card.strokeColor = outlineColor
            holder.card.cardElevation = 0f
        }

        holder.itemView.setOnClickListener {
            selectedId = kategori?.id ?: 0
            notifyDataSetChanged()
            onClick(kategori)
        }
    }

    override fun getItemCount() = items.size + 1

    fun updateKategori(newList: List<Kategori>) {
        items = newList
        notifyDataSetChanged()
    }
}

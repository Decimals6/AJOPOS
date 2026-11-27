 package com.ajoclub.ajopos.view.adapter.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajoclub.ajopos.databinding.ItemNotaCardBinding
import com.ajoclub.ajopos.model.data.Nota
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class NotaListAdapter(
    private val onClick: (Nota) -> Unit
) : ListAdapter<Nota, NotaListAdapter.NotaViewHolder>(NotaDiffCallback()) {

    class NotaViewHolder(val binding: ItemNotaCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(nota: Nota, onClick: (Nota) -> Unit) {
            val shortId = if (nota.localId.length > 18) nota.localId.take(18) + "…" else nota.localId
            binding.tvUuid.text = shortId

            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("in","ID"))
            binding.tvDate.text = sdf.format(Date(nota.waktu))

            binding.tvPelanggan.text = nota.pelanggan ?: "-"

            val nf = NumberFormat.getCurrencyInstance(Locale("in","ID")).apply { maximumFractionDigits = 0 }
            binding.tvTotal.text = nf.format(nota.total)

            binding.root.setOnClickListener {
                onClick(nota)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val binding = ItemNotaCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }
}

class NotaDiffCallback : DiffUtil.ItemCallback<Nota>() {
    override fun areItemsTheSame(oldItem: Nota, newItem: Nota): Boolean =
        oldItem.localId == newItem.localId

    override fun areContentsTheSame(oldItem: Nota, newItem: Nota): Boolean =
        oldItem == newItem
}
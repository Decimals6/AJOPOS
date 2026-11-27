package com.ajoclub.ajopos.view.adapter.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ajoclub.ajopos.databinding.ItemKategoriCardBinding
import com.ajoclub.ajopos.model.data.Kategori
import com.ajoclub.ajopos.viewmodel.KategoriViewModel
import androidx.appcompat.app.AlertDialog


class KategoriAdapter(
    private var kategoriList: MutableList<Kategori>,
    private val onItemClick: ((Kategori) -> Unit)? = null,
    private val viewModel: KategoriViewModel
) : RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder>() {

    class KategoriViewHolder(val binding: ItemKategoriCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val binding = ItemKategoriCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return KategoriViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        val kategori = kategoriList[position]
        holder.binding.apply {
            tvNamaKategori.text = kategori.nama
            tvDeskripsiKategori.text = kategori.keterangan

            // tombol delete ditekan
            btnDelete.setOnClickListener {
                val context = holder.itemView.context

                AlertDialog.Builder(context)
                    .setTitle("Hapus Kategori")
                    .setMessage("Apakah Anda yakin ingin menghapus kategori \"${kategori.nama}\"?")
                    .setPositiveButton("Iya") { dialog, _ ->
                        // panggil viewmodel delete
                        viewModel.deleteKategori(kategori)

                        // hapus dari list adapter biar langsung hilang dari UI
                        val newList = kategoriList.toMutableList()
                        newList.removeAt(position)
                        kategoriList = newList
                        notifyItemRemoved(position)

                        dialog.dismiss()
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        // listener buat klik item (misal edit/detail)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(kategori)
        }
    }


    override fun getItemCount(): Int = kategoriList.size

    fun updateKategoriList(newList: List<Kategori>) {
        kategoriList.clear()
        kategoriList.addAll(newList)
        notifyDataSetChanged()
    }
}

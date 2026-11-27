package com.ajoclub.ajopos.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Produk(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val kategoriId: Int,
    val nama: String,
    val barcode: String? = null,
    val gambar: String? = null,
    val harga: Double,
    val keterangan: String,
    val created_at: Long,
    val updated_at: Long,
    val deleted: Int = 0
)
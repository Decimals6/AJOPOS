package com.ajoclub.ajopos.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DetailNota(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "nota_local_id") val notaLocalId: String, // FK -> Nota.local_id (String)
    @ColumnInfo(name = "produk_id") val produkId: Int,
    val keterangan: String? = null,
    val harga: Double,
    val jumlah: Int,
    val subtotal: Double,
    val sync: Int = 0
)

package com.ajoclub.ajopos.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Kategori(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val nama: String,
    val keterangan: String,
    val created_at: Long, //unix timestamp
    val updated_at: Long,
    val deleted: Int
)

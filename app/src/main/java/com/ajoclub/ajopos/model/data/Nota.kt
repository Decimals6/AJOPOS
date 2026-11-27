package com.ajoclub.ajopos.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Nota(
    @PrimaryKey @ColumnInfo(name = "local_id") val localId: String, // client-generated
    @ColumnInfo(name = "server_id") val serverId: Int? = null,
    @ColumnInfo(name = "user_id") val userId: Int,
    val pelanggan: String? = null,
    val waktu: Long,
    val tipe: String,
    val total: Double,
    val pembayaran: String,
    val sync: Int = 0,
    val created_at: Long,
    val updated_at: Long,
    val deleted: Int = 0
)

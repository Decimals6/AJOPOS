package com.ajoclub.ajopos.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "print_job")
data class PrintJob(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val notaLocalId: String,
    val target: String,
    val printerId: String?,
    val copies: Int,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val payload: ByteArray,

    val status: String,
    val attempts: Int,
    val errorMsg: String? = null,
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis()
)


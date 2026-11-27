package com.ajoclub.ajopos.model.data

data class DetailNotaDisplay(
    val id: Int,
    val produkId: Int,
    val produkNama: String,
    val keterangan: String?,
    val harga: Double,
    val jumlah: Int,
    val subtotal: Double
)
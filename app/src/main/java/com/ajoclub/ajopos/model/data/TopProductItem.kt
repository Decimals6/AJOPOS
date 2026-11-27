package com.ajoclub.ajopos.model.data


data class TopProductItem(
    val produkId: Int,
    val totalQty: Int,
    val totalRevenue: Double
)

data class TopProductDisplay(
    val nama: String,
    val totalQty: Int,
    val totalRevenue: Double,
    val percentage: Int // untuk bar visual
)

data class ReportSummary(
    val totalOmzet: Double,
    val totalTransaksi: Int,
    val rataRata: Double
)
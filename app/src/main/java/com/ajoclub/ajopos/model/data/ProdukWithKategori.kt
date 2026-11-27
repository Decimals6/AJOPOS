package com.ajoclub.ajopos.model.data

import androidx.room.Embedded
import androidx.room.Relation

data class ProdukWithKategori(
    @Embedded val produk: Produk,

    @Relation(
        parentColumn = "kategoriId",
        entityColumn = "id"
    )
    val kategori: Kategori
)

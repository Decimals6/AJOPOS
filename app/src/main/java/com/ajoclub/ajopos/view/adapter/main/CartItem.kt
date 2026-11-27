package com.ajoclub.ajopos.model.data

import java.io.Serializable

data class CartItem(
    val produk: ProdukWithKategori,
    var quantity: Int = 1,
    var note: String? = null
) : Serializable

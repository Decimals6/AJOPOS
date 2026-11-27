package com.ajoclub.ajopos.repository

import com.ajoclub.ajopos.model.data.Kategori
import com.ajoclub.ajopos.model.data.Produk
import com.ajoclub.ajopos.model.data.ProdukWithKategori
import com.ajoclub.ajopos.model.dao.KategoriDao
import com.ajoclub.ajopos.model.dao.ProdukDao

class MenuRepository(
    private val produkDao: ProdukDao,
    private val kategoriDao: KategoriDao
) {
    // ---------- PRODUK ----------
    suspend fun getAllProduk(): List<Produk> = produkDao.getAllProduk()

    suspend fun getAllProdukWithKategori(): List<ProdukWithKategori> =
        produkDao.getAllProdukWithKategori()

//    suspend fun insertAllProduk(produks: List<Produk>) {
//        produkDao.insert(produks)
//    }

//    suspend fun clearAllProduk() {
//        produkDao.clearAll()
//    }

    // ---------- KATEGORI ----------
    suspend fun getAllKategori(): List<Kategori> = kategoriDao.getAllActive()

//    suspend fun insertAllKategori(kategoris: List<Kategori>) {
//        kategoriDao.insertAll(kategoris)
//    }
//
//    suspend fun clearAllKategori() {
//        kategoriDao.clearAll()
//    }
}

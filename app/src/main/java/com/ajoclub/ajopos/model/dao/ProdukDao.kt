package com.ajoclub.ajopos.model.dao

import androidx.room.*
import com.ajoclub.ajopos.model.data.Produk
import com.ajoclub.ajopos.model.data.ProdukWithKategori

@Dao
interface ProdukDao {
    @Insert
    fun insert(produk: Produk)

    @Update
    fun update(produk: Produk)

    @Delete
    fun delete(produk: Produk)

    @Query("SELECT * FROM Produk WHERE deleted = 0")
    fun getAllProduk(): List<Produk>

    @Transaction
    @Query("SELECT * FROM Produk WHERE deleted = 0")
    fun getAllProdukWithKategori(): List<ProdukWithKategori>

    @Transaction
    @Query("SELECT * FROM Produk WHERE id = :id LIMIT 1")
    fun getProdukWithKategoriById(id: Int): ProdukWithKategori?

    @Transaction
    @Query("SELECT * FROM Produk WHERE barcode = :barcode AND deleted = 0 LIMIT 1")
    fun getProdukWithKategoriByBarcode(barcode: String): ProdukWithKategori?

    @Query("UPDATE Produk SET deleted = 1 WHERE id = :id")
    fun softDelete(id: Int)

    @Query("SELECT * FROM Produk WHERE id = :id LIMIT 1")
    fun getProdukById(id: Int): Produk?

    @Query("SELECT * FROM Produk WHERE id IN (:ids)")
    fun getProdukByIds(ids: List<Int>): List<Produk>
}

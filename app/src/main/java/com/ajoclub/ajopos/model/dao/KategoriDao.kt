package com.ajoclub.ajopos.model.dao

import androidx.room.*
import com.ajoclub.ajopos.model.data.Kategori

@Dao
interface KategoriDao {

    @Insert
    fun insert(kategori: Kategori)

    @Update
    fun update(kategori: Kategori)

    @Delete
    fun delete(kategori: Kategori)

    @Query("SELECT * FROM Kategori WHERE deleted = 0")
    fun getAllActive(): List<Kategori>

    @Query("SELECT * FROM Kategori WHERE id = :id LIMIT 1")
    fun getKategoriById(id: Int): Kategori?

    @Query("UPDATE Kategori SET deleted = 1 WHERE id = :id")
    fun softDelete(id: Int)
}

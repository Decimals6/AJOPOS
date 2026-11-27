package com.ajoclub.ajopos.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ajoclub.ajopos.model.data.DetailNota

@Dao
interface DetailNotaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(details: List<DetailNota>)

    @Query("SELECT * FROM DetailNota WHERE nota_local_id = :notaLocalId")
    fun getByNotaLocalId(notaLocalId: Int): List<DetailNota>

    @Query("DELETE FROM DetailNota WHERE nota_local_id = :notaLocalId")
    fun deleteByNota(notaLocalId: Int)
}

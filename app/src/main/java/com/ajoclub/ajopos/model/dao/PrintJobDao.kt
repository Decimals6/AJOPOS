// app/src/main/java/com/example/petungpos/model/dao/PrintJobDao.kt
package com.ajoclub.ajopos.model.dao

import androidx.room.*
import com.ajoclub.ajopos.model.data.PrintJob

@Dao
interface PrintJobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(printJob: PrintJob): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMany(jobs: List<PrintJob>)

    @Query("SELECT * FROM print_job WHERE status = :status ORDER BY created_at ASC")
    fun getByStatus(status: String): List<PrintJob>

    @Query("SELECT * FROM print_job WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT :limit")
    fun getPending(limit: Int): List<PrintJob>

    @Query("SELECT * FROM print_job WHERE id = :id LIMIT 1")
    fun getById(id: Int): PrintJob?

    @Update
    fun update(printJob: PrintJob)

    @Delete
    fun delete(printJob: PrintJob)

    @Query("DELETE FROM print_job WHERE status = :status")
    fun deleteAllByStatus(status: String)

    @Query("SELECT * FROM print_job WHERE notaLocalId = :localId")
    fun getByNotaLocalId(localId: String): List<PrintJob>
}
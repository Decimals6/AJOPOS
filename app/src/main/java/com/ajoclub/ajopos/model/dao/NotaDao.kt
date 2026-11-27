package com.ajoclub.ajopos.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ajoclub.ajopos.model.data.Nota
import com.ajoclub.ajopos.model.data.NotaWithDetail
import com.ajoclub.ajopos.model.data.TopProductItem

@Dao
interface NotaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(nota: Nota)

    @Update
    fun update(nota: Nota)

    @Transaction
    @Query("SELECT * FROM Nota WHERE deleted = 0 ORDER BY waktu DESC")
    fun getAllWithDetails(): List<NotaWithDetail>

    @Query("SELECT * FROM Nota WHERE sync = 0 AND deleted = 0")
    fun getUnsynced(): List<Nota>

    @Query("UPDATE Nota SET sync = 1 WHERE local_id = :localId")
    fun markSynced(localId: String)

    @Query("DELETE FROM Nota")
    fun clearAll()

    @Query("SELECT * FROM nota WHERE deleted = 0 ORDER BY waktu DESC")
    fun getAllNotas(): List<Nota>

    @Query("SELECT * FROM nota WHERE local_id = :localId LIMIT 1")
    fun getNotaWithDetailsByLocalId(localId: String): NotaWithDetail?

    @Query("""
        SELECT COALESCE(SUM(total), 0.0) 
        FROM Nota 
        WHERE deleted = 0 AND waktu BETWEEN :startDate AND :endDate
    """)
    fun getOmzetByRange(startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COUNT(local_id) 
        FROM Nota 
        WHERE deleted = 0 AND waktu BETWEEN :startDate AND :endDate
    """)
    fun countTransaksiByRange(startDate: Long, endDate: Long): Int

    @Query("""
        SELECT dn.produk_id AS produkId, 
               SUM(dn.jumlah) AS totalQty, 
               SUM(dn.subtotal) AS totalRevenue
        FROM DetailNota dn
        INNER JOIN Nota n ON dn.nota_local_id = n.local_id
        WHERE n.deleted = 0 AND n.waktu BETWEEN :startDate AND :endDate
        GROUP BY dn.produk_id
        ORDER BY totalQty DESC
        LIMIT 5
    """)
    fun getTopSellingProducts(startDate: Long, endDate: Long): List<TopProductItem>

    @Transaction
        @Query("""
        SELECT * FROM Nota 
        WHERE deleted = 0 AND waktu BETWEEN :startDate AND :endDate 
        ORDER BY waktu DESC
    """)
    fun getNotasWithDetailsByRange(startDate: Long, endDate: Long): List<NotaWithDetail>

}

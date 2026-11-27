package com.ajoclub.ajopos.repository

import com.ajoclub.ajopos.model.data.DetailNota
import com.ajoclub.ajopos.model.data.Nota
import com.ajoclub.ajopos.model.data.NotaWithDetail
import com.ajoclub.ajopos.model.dao.DetailNotaDao
import com.ajoclub.ajopos.model.dao.NotaDao

class TransaksiRepository(
    private val notaDao: NotaDao,
    private val detailNotaDao: DetailNotaDao
) {
    // ---------- NOTA ----------
    suspend fun insertNota(nota: Nota) {
        notaDao.insert(nota)
    }

    suspend fun updateNota(nota: Nota) {
        notaDao.update(nota)
    }

    suspend fun getAllNotaWithDetails(): List<NotaWithDetail> =
        notaDao.getAllWithDetails()

    suspend fun getUnsyncedNota(): List<Nota> = notaDao.getUnsynced()

    suspend fun markNotaSynced(localId: String) {
        notaDao.markSynced(localId)
    }

    suspend fun clearAllNota() {
        notaDao.clearAll()
    }

    // ---------- DETAIL NOTA ----------
    suspend fun insertAllDetail(details: List<DetailNota>) {
        detailNotaDao.insertAll(details)
    }

    suspend fun getDetailByNotaLocalId(notaLocalId: Int): List<DetailNota> =
        detailNotaDao.getByNotaLocalId(notaLocalId)

    suspend fun deleteDetailByNota(notaLocalId: Int) {
        detailNotaDao.deleteByNota(notaLocalId)
    }
}

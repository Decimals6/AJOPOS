package com.ajoclub.ajopos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ajoclub.ajopos.bt.ReceiptFormatter
import com.ajoclub.ajopos.model.data.DetailNotaDisplay
import com.ajoclub.ajopos.model.data.Nota
import com.ajoclub.ajopos.model.data.PrintJobStatus
import com.ajoclub.ajopos.model.database.AppDatabase
import com.ajoclub.ajopos.util.Event
import com.ajoclub.ajopos.util.PrinterPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotaDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _nota = MutableLiveData<Nota?>()
    val nota: LiveData<Nota?> = _nota

    val reprintStatus = MutableLiveData<Event<String>>()
    private val appCtx = application.applicationContext
    private val reprintLock = Any()
    private val reprintsInProgress = mutableSetOf<String>()

    private val _items = MutableLiveData<List<DetailNotaDisplay>>(emptyList())
    val items: LiveData<List<DetailNotaDisplay>> = _items

    /**
     * Load NotaWithDetail, then fetch all Produk in batch and map to DetailNotaDisplay.
     */
    fun loadNotaAndItems(localId: String) {
        viewModelScope.launch {
            val notaWithDetail = withContext(Dispatchers.IO) {
                db.notaDao().getNotaWithDetailsByLocalId(localId)
            }

            if (notaWithDetail == null) {
                _nota.postValue(null)
                _items.postValue(emptyList())
                return@launch
            }

            _nota.postValue(notaWithDetail.nota)

            // collect product IDs
            val prodIds = notaWithDetail.detailNota.map { it.produkId }.distinct()
            val produkList = if (prodIds.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    db.produkDao().getProdukByIds(prodIds)
                }
            } else emptyList()

            // map id -> Produk for fast lookup
            val produkMap = produkList.associateBy { it.id }

            // build display list
            val displays = notaWithDetail.detailNota.map { d ->
                val nama = produkMap[d.produkId]?.nama ?: "Produk#${d.produkId}"
                DetailNotaDisplay(
                    id = d.id,
                    produkId = d.produkId,
                    produkNama = nama,
                    keterangan = d.keterangan,
                    harga = d.harga,
                    jumlah = d.jumlah,
                    subtotal = d.subtotal
                )
            }

            _items.postValue(displays)
        }
    }

    fun enqueueReprint(localId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldJobs = db.printJobDao().getByNotaLocalId(localId)
            if (oldJobs.isEmpty()) return@launch

            val now = System.currentTimeMillis()

            val newJobs = oldJobs.map { job ->
                job.copy(
                    id = 0, // supaya Room bikin row baru
                    status = PrintJobStatus.PENDING,
                    attempts = 0,
                    errorMsg = null,
                    created_at = now,
                    updated_at = now
                )
            }

            db.printJobDao().insertMany(newJobs)
        }
    }

    fun reprintDirect(localId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // 0) Prevent duplicate reprint for same nota
            synchronized(reprintLock) {
                if (reprintsInProgress.contains(localId)) {
                    reprintStatus.postValue(Event("Reprint sudah berjalan untuk nota ini"))
                    return@launch
                }
                reprintsInProgress.add(localId)
            }

            try {
                // 1) Load nota + items lewat relation
                val notaWithDetail = db.notaDao().getNotaWithDetailsByLocalId(localId)
                if (notaWithDetail == null) {
                    reprintStatus.postValue(Event("Nota tidak ditemukan"))
                    return@launch
                }

                val nota = notaWithDetail.nota
                val details = notaWithDetail.detailNota
                if (details.isEmpty()) {
                    reprintStatus.postValue(Event("Detail nota kosong"))
                    return@launch
                }

                // 2) Join produk -> DetailNotaDisplay
                val produkIds = details.map { it.produkId }.distinct()
                val produkList = if (produkIds.isNotEmpty()) db.produkDao().getProdukByIds(produkIds) else emptyList()
                val produkMap = produkList.associateBy { it.id }

                val displayItems = details.map { d ->
                    com.ajoclub.ajopos.model.data.DetailNotaDisplay(
                        id = d.id,
                        produkId = d.produkId,
                        produkNama = produkMap[d.produkId]?.nama ?: "Produk ${d.produkId}",
                        keterangan = d.keterangan,
                        harga = d.harga,
                        jumlah = d.jumlah,
                        subtotal = d.subtotal
                    )
                }

                // 3) Periksa mode printer
                val mode = PrinterPrefs.getPrinterMode(appCtx)
                if (mode == "none") {
                    reprintStatus.postValue(Event("Printer belum dikonfigurasi"))
                    return@launch
                }

                val jobs = mutableListOf<com.ajoclub.ajopos.model.data.PrintJob>()

                // 4) CUSTOMER RECEIPT JOB
                if (mode == "single" || mode == "double") {
                    val payloadCustomer = ReceiptFormatter.buildCustomerReceipt(appCtx, nota, displayItems)
                    jobs.add(
                        com.ajoclub.ajopos.model.data.PrintJob(
                            notaLocalId = nota.localId,
                            target = "CUSTOMER",
                            printerId = null,
                            copies = 1,
                            payload = payloadCustomer,
                            status = com.ajoclub.ajopos.model.data.PrintJobStatus.PENDING,
                            attempts = 0
                        )
                    )
                }

                // 5) KITCHEN RECEIPT JOB
                if (mode == "double") {
                    val payloadKitchen = ReceiptFormatter.buildKitchenReceipt(appCtx, nota, displayItems)
                    jobs.add(
                        com.ajoclub.ajopos.model.data.PrintJob(
                            notaLocalId = nota.localId,
                            target = "KITCHEN",
                            printerId = null,
                            copies = 1,
                            payload = payloadKitchen,
                            status = com.ajoclub.ajopos.model.data.PrintJobStatus.PENDING,
                            attempts = 0
                        )
                    )
                }

                // 6) Simpan ke DB & Trigger Worker
                if (jobs.isNotEmpty()) {
                    db.printJobDao().insertMany(jobs)
                    com.ajoclub.ajopos.worker.PrintJobWorker.enqueueNow(appCtx)
                    reprintStatus.postValue(Event("SUCCESS"))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                reprintStatus.postValue(Event(e.message ?: "Gagal cetak ulang"))
            } finally {
                synchronized(reprintLock) {
                    reprintsInProgress.remove(localId)
                }
            }
        }
    }
}
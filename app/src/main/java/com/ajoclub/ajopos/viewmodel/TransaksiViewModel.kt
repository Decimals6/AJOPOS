package com.ajoclub.ajopos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ajoclub.ajopos.model.data.*
import com.ajoclub.ajopos.model.database.AppDatabase
import com.ajoclub.ajopos.util.PrinterPrefs
import com.ajoclub.ajopos.bt.ReceiptFormatter
import com.ajoclub.ajopos.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.ajoclub.ajopos.worker.PrintJobWorker

class TransaksiViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val appCtx = application.applicationContext
    private val printJobWorker = PrintJobWorker

    val createdLocalId = MutableLiveData<Event<String>>()
    val error = MutableLiveData<Event<String>>()

    private fun normalizeTipe(tipeRaw: String?): String {
        return when (tipeRaw?.trim()?.uppercase()) {
            "DINE_IN", "DINE-IN", "DINE IN" -> "DINE_IN"
            "TAKEAWAY", "TAKE-AWAY", "TAKE AWAY", "TAKEAWAY" -> "TAKEAWAY"
            else -> "TAKEAWAY"
        }
    }

    fun createNotaWithDetailsAndPrintJobs(
        nota: Nota,
        details: List<DetailNota>,
        cash: String? = null
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {

                    // Normalize tipe to avoid accidental typos
                    val tipeNormalized = normalizeTipe(nota.tipe)
                    val notaToSave = nota.copy(tipe = tipeNormalized)

                    // 1) JOIN DetailNota ke Produk → DetailNotaDisplay
                    val produkIds = details.map { it.produkId }.distinct()
                    val produkList = if (produkIds.isNotEmpty()) {
                        db.produkDao().getProdukByIds(produkIds)
                    } else emptyList()

                    val produkMap = produkList.associateBy { it.id }

                    val displayItems: List<DetailNotaDisplay> = details.map { d ->
                        val p = produkMap[d.produkId]
                        DetailNotaDisplay(
                            id = 0,
                            produkId = d.produkId,
                            produkNama = p?.nama ?: "Produk ${d.produkId}",
                            keterangan = d.keterangan,
                            harga = d.harga,
                            jumlah = d.jumlah,
                            subtotal = d.subtotal
                        )
                    }

                    db.runInTransaction {
                        // 2) Simpan Nota & Detail
                        db.notaDao().insert(notaToSave)
                        db.detailNotaDao().insertAll(details)

                        // 3) Mode printer
                        val mode = PrinterPrefs.getPrinterMode(appCtx) // none | single | double

                        if (mode != "none") {
                            val jobs = mutableListOf<PrintJob>()

                            // CUSTOMER RECEIPT
                            if (mode == "single" || mode == "double") {
                                val payloadCustomer: ByteArray =
                                    ReceiptFormatter.buildCustomerReceipt(appCtx, notaToSave, displayItems, cash)

                                jobs.add(
                                    PrintJob(
                                        notaLocalId = notaToSave.localId,
                                        target = "CUSTOMER",
                                        printerId = null,
                                        copies = 1,
                                        payload = payloadCustomer,
                                        status = PrintJobStatus.PENDING,
                                        attempts = 0
                                    )
                                )
                            }

                            // KITCHEN RECEIPT
                            if (mode == "double") {
                                val payloadKitchen: ByteArray =
                                    ReceiptFormatter.buildKitchenReceipt(appCtx, notaToSave, displayItems)

                                jobs.add(
                                    PrintJob(
                                        notaLocalId = notaToSave.localId,
                                        target = "KITCHEN",
                                        printerId = null,
                                        copies = 1,
                                        payload = payloadKitchen,
                                        status = PrintJobStatus.PENDING,
                                        attempts = 0
                                    )
                                )
                            }

                            if (jobs.isNotEmpty()) {
                                db.printJobDao().insertMany(jobs)
                                printJobWorker.enqueueNow(appCtx)
                            }
                        }
                    }
                }

                createdLocalId.postValue(Event(nota.localId))

            } catch (e: Exception) {
                e.printStackTrace()
                error.postValue(Event(e.message ?: "Unknown error"))
            }
        }
    }

    // debug helper (kalo perlu)
    fun debugNotaDisplayMapping(
        nota: Nota,
        details: List<DetailNota>
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {

                    println("==== DEBUG: START ====")

                    // 1) Log detail raw dari parameter
                    println("DETAIL RAW (${details.size} items):")
                    details.forEach { d ->
                        println(" - Detail: produkId=${d.produkId}, jml=${d.jumlah}, harga=${d.harga}, subtotal=${d.subtotal}, ket=${d.keterangan}")
                    }

                    // 2) Query produk yg terlibat
                    val produkIds = details.map { it.produkId }.distinct()
                    println("PRODUK IDs: $produkIds")

                    val produkList = if (produkIds.isNotEmpty()) {
                        db.produkDao().getProdukByIds(produkIds)
                    } else emptyList()

                    println("PRODUK RESULT (${produkList.size} items):")
                    produkList.forEach { p ->
                        println(" - Produk: id=${p.id}, nama=${p.nama}, harga=${p.harga}")
                    }

                    val produkMap = produkList.associateBy { it.id }

                    // 3) Mapping ke DetailNotaDisplay
                    val displayItems: List<DetailNotaDisplay> = details.map { d ->
                        val p = produkMap[d.produkId]
                        DetailNotaDisplay(
                            id = 0,
                            produkId = d.produkId,
                            produkNama = p?.nama ?: "(NOT FOUND id=${d.produkId})",
                            keterangan = d.keterangan,
                            harga = d.harga,
                            jumlah = d.jumlah,
                            subtotal = d.subtotal
                        )
                    }

                    println("DISPLAY ITEMS (${displayItems.size} items):")
                    displayItems.forEach { di ->
                        println(" - Display: nama=${di.produkNama}, jml=${di.jumlah}, sub=${di.subtotal}")
                    }

                    if (displayItems.isEmpty()) {
                        println("⚠ WARNING: displayItems KOSONG!")
                    }

                    // 4) Extra validation
                    val missingProducts = displayItems.filter { it.produkNama.startsWith("(NOT FOUND") }
                    if (missingProducts.isNotEmpty()) {
                        println("⚠ ADA PRODUK YG GA KETEMU DI DB:")
                        missingProducts.forEach { println("    - produkId missing: ${it.produkId}") }
                    }

                    println("==== DEBUG: END ====")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR DEBUG MAPPING: ${e.message}")
            }
        }
    }
}
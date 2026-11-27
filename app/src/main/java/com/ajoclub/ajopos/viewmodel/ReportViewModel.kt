package com.ajoclub.ajopos.ui.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ajoclub.ajopos.model.data.ReportSummary
import com.ajoclub.ajopos.model.data.TopProductDisplay
import com.ajoclub.ajopos.model.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val appCtx = application.applicationContext

    private val _summary = MutableLiveData<ReportSummary>()
    val summary: LiveData<ReportSummary> = _summary

    private val _topProducts = MutableLiveData<List<TopProductDisplay>>()
    val topProducts: LiveData<List<TopProductDisplay>> = _topProducts

    private val _selectedDateRangeText = MutableLiveData<String>()
    val selectedDateRangeText: LiveData<String> = _selectedDateRangeText

    private val _csvExportFile = MutableLiveData<File?>()
    val csvExportFile: LiveData<File?> = _csvExportFile

    var currentStartDate: Long = 0L
    var currentEndDate: Long = 0L

    init {
        setTodayFilter()
    }

    fun setTodayFilter() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis

        setDateRange(start, end, "Hari Ini")
    }

    fun setDateRange(startDate: Long, endDate: Long, label: String? = null) {
        currentStartDate = startDate
        currentEndDate = endDate

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        _selectedDateRangeText.value = label ?: "${sdf.format(Date(startDate))} - ${sdf.format(Date(endDate))}"

        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            val omzet = withContext(Dispatchers.IO) {
                db.notaDao().getOmzetByRange(currentStartDate, currentEndDate)
            }
            val count = withContext(Dispatchers.IO) {
                db.notaDao().countTransaksiByRange(currentStartDate, currentEndDate)
            }
            val avg = if (count > 0) omzet / count else 0.0
            _summary.value = ReportSummary(omzet, count, avg)

            // Top Products
            val rawTop = withContext(Dispatchers.IO) {
                db.notaDao().getTopSellingProducts(currentStartDate, currentEndDate)
            }

            if (rawTop.isNotEmpty()) {
                val prodIds = rawTop.map { it.produkId }
                val produkList = withContext(Dispatchers.IO) {
                    db.produkDao().getProdukByIds(prodIds)
                }
                val prodMap = produkList.associateBy { it.id }
                val maxQty = rawTop.maxOfOrNull { it.totalQty } ?: 1

                val displayList = rawTop.map { item ->
                    val nama = prodMap[item.produkId]?.nama ?: "Produk #${item.produkId}"
                    val percentage = ((item.totalQty.toDouble() / maxQty) * 100).toInt()
                    TopProductDisplay(nama, item.totalQty, item.totalRevenue, percentage)
                }
                _topProducts.value = displayList
            } else {
                _topProducts.value = emptyList()
            }
        }
    }

    fun exportCsv() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = db.notaDao().getNotasWithDetailsByRange(currentStartDate, currentEndDate)
            if (list.isEmpty()) {
                _csvExportFile.postValue(null)
                return@launch
            }

            // Kumpulkan nama produk
            val allProdIds = list.flatMap { it.detailNota.map { d -> d.produkId } }.distinct()
            val prodMap = if (allProdIds.isNotEmpty()) {
                db.produkDao().getProdukByIds(allProdIds).associateBy { it.id }
            } else emptyMap()

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val exportDir = File(appCtx.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val csvFile = File(exportDir, "Laporan_Penjualan_${System.currentTimeMillis()}.csv")
            csvFile.bufferedWriter().use { writer ->
                // Header CSV
                writer.write("Local ID,Waktu,Pelanggan,Tipe,Metode Bayar,Total Nota,Nama Produk,Qty,Harga Satuan,Subtotal\n")

                for (item in list) {
                    val n = item.nota
                    val waktuStr = sdf.format(Date(n.waktu))
                    val pelanggan = n.pelanggan ?: "-"

                    for (d in item.detailNota) {
                        val prodName = (prodMap[d.produkId]?.nama ?: "Produk #${d.produkId}").replace(",", " ")
                        writer.write("${n.localId},$waktuStr,$pelanggan,${n.tipe},${n.pembayaran},${n.total.toLong()},$prodName,${d.jumlah},${d.harga.toLong()},${d.subtotal.toLong()}\n")
                    }
                }
            }

            _csvExportFile.postValue(csvFile)
        }
    }

    fun resetCsvFile() {
        _csvExportFile.value = null
    }
}
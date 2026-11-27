package com.ajoclub.ajopos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ajoclub.ajopos.model.data.Kategori
import com.ajoclub.ajopos.model.data.Produk
import com.ajoclub.ajopos.model.data.ProdukWithKategori
import com.ajoclub.ajopos.model.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProdukViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val produkDao = db.produkDao()

    val produkLD = MutableLiveData<List<ProdukWithKategori>>()
    val kategoriLD = MutableLiveData<List<Kategori>>()
    val selectedProdukLD = MutableLiveData<ProdukWithKategori?>()
    val scannedProdukLD = MutableLiveData<ProdukWithKategori?>()

    private var selectedKategoriId: Int? = null
    private var fullList: List<ProdukWithKategori> = emptyList()

    fun getAllKategori() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = db.kategoriDao().getAllActive()
            kategoriLD.postValue(list)
        }
    }

    fun getAllProduk() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = produkDao.getAllProdukWithKategori()
            fullList = list
            produkLD.postValue(list)
        }
    }

    fun filterByKategori(kategoriId: Int?) {
        selectedKategoriId = kategoriId
        val filtered = fullList.filter { pwk ->
            kategoriId == null || pwk.kategori.id == kategoriId
        }
        produkLD.postValue(filtered)
    }

    fun filterProduk(query: String) {
        val q = query.trim()
        val filtered = fullList.filter { pwk ->
            val matchKategori = selectedKategoriId == null || pwk.kategori.id == selectedKategoriId
            val matchText = pwk.produk.nama.contains(q, ignoreCase = true) ||
                    (pwk.produk.barcode?.contains(q, ignoreCase = true) == true)
            matchKategori && matchText
        }
        produkLD.postValue(filtered)
    }

    fun findByBarcode(barcode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = produkDao.getProdukWithKategoriByBarcode(barcode)
            scannedProdukLD.postValue(item)
        }
    }

    fun getProdukById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = produkDao.getProdukWithKategoriById(id)
            selectedProdukLD.postValue(item)
        }
    }

    fun addProduk(produk: Produk) {
        viewModelScope.launch(Dispatchers.IO) {
            produkDao.insert(produk)
        }
    }

    fun updateProduk(produk: Produk) {
        viewModelScope.launch(Dispatchers.IO) {
            produkDao.update(produk)
        }
    }

    fun deleteProduk(produk: Produk) {
        viewModelScope.launch(Dispatchers.IO) {
            produkDao.softDelete(produk.id)
        }
    }
}

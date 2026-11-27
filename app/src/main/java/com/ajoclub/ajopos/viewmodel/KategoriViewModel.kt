package com.ajoclub.ajopos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ajoclub.ajopos.model.data.Kategori
import com.ajoclub.ajopos.model.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class KategoriViewModel(application: Application) : AndroidViewModel(application), CoroutineScope {

    private val kategoriDao = AppDatabase.getDatabase(application).kategoriDao()
    val kategoriLD = MutableLiveData<List<Kategori>>()
    val selectedKategoriLD = MutableLiveData<Kategori?>()
    private var job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    fun getAllKategori() {
        launch {
            val list = kategoriDao.getAllActive()
            kategoriLD.postValue(list)
        }
    }

    fun getKategoriById(id: Int) {
        launch {
            val kategori = kategoriDao.getKategoriById(id)
            selectedKategoriLD.postValue(kategori)
        }
    }

    fun addKategori(kategori: Kategori) {
        viewModelScope.launch(Dispatchers.IO) {
            kategoriDao.insert(kategori)
        }
    }

    fun updateKategori(kategori: Kategori) {
        viewModelScope.launch(Dispatchers.IO) {
            kategoriDao.update(kategori)
        }
    }

    fun deleteKategori(kategori: Kategori) {
        viewModelScope.launch(Dispatchers.IO) {
            kategoriDao.softDelete(kategori.id)
            getAllKategori()
        }
    }
}

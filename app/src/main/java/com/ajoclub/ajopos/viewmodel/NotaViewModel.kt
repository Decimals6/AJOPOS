package com.ajoclub.ajopos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ajoclub.ajopos.model.data.Nota
import com.ajoclub.ajopos.model.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _notas = MutableLiveData<List<Nota>>(emptyList())
    val notas: LiveData<List<Nota>> = _notas

    // load all (desc by waktu)
    fun loadNotas() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                // rely on NotaDao.getAllNotas() — add it to your NotaDao if not present
                db.notaDao().getAllNotas()
            }
            _notas.postValue(list)
        }
    }
}
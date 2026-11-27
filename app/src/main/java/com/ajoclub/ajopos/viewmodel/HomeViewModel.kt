package com.ajoclub.ajopos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ajoclub.ajopos.model.data.CartItem

class HomeViewModel : ViewModel() {

    private val _cartItems = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cartItems: LiveData<MutableList<CartItem>> get() = _cartItems

    fun addToCart(item: CartItem) {
        val current = _cartItems.value ?: mutableListOf()
        current.add(item)
        _cartItems.value = current
    }

    fun clearCart() {
        _cartItems.value = mutableListOf()
    }
}


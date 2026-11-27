package com.ajoclub.ajopos.util

import android.content.Context
import androidx.core.content.edit

object PrinterPrefs {

    private const val PREF_NAME = "printer_prefs"
    private const val KEY_CUSTOMER_MAC = "printer_customer_mac"
    private const val KEY_DAPUR_MAC = "printer_dapur_mac"
    private const val KEY_PRINTER_MODE = "printer_mode" // none | single | double

    // =====================
    // RECEIPT / STORE CONFIG
    // =====================
    private const val KEY_STORE_NAME = "printer_store_name"
    private const val KEY_STORE_ADDRESS = "printer_store_address"
    private const val KEY_STORE_PHONE = "printer_store_phone"
    private const val KEY_PAPER_WIDTH = "printer_paper_width"

    const val PAPER_58_NATIVE = 58
    const val PAPER_80_NATIVE = 80
    const val PAPER_58_ON_80 = 5880

    private const val DEFAULT_STORE_NAME = "AJOCLUB"
    private const val DEFAULT_STORE_ADDRESS = "Jl. Intan 3 no 42\nKec. Paras, Mulung"
    private const val DEFAULT_STORE_PHONE = "+12345678910"
    private const val DEFAULT_PAPER_WIDTH = PAPER_58_NATIVE

    // =====================
    // PRINTER MODE
    // =====================
    fun savePrinterMode(ctx: Context, mode: String) {
        val sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit { putString(KEY_PRINTER_MODE, mode) }
    }

    fun getPrinterMode(ctx: Context): String {
        val sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sp.getString(KEY_PRINTER_MODE, "none") ?: "none"
    }

    fun isNoneMode(ctx: Context) = getPrinterMode(ctx) == "none"
    fun isSingleMode(ctx: Context) = getPrinterMode(ctx) == "single"
    fun isDoubleMode(ctx: Context) = getPrinterMode(ctx) == "double"

    // =====================
    // SAVE PRINTER MAC
    // =====================
    fun saveCustomerPrinter(ctx: Context, mac: String) {
        val sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit { putString(KEY_CUSTOMER_MAC, mac) }
    }

    fun saveDapurPrinter(ctx: Context, mac: String) {
        val sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit { putString(KEY_DAPUR_MAC, mac) }
    }

    // =====================
    // GET PRINTER MAC
    // =====================
    fun getCustomerPrinter(ctx: Context): String? {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CUSTOMER_MAC, null)
    }

    fun getDapurPrinter(ctx: Context): String? {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DAPUR_MAC, null)
    }

    // =====================
    // RECEIPT CONFIG
    // =====================
    fun saveStoreName(ctx: Context, name: String) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_STORE_NAME, name) }
    }

    fun getStoreName(ctx: Context): String {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_STORE_NAME, DEFAULT_STORE_NAME) ?: DEFAULT_STORE_NAME
    }

    fun saveStoreAddress(ctx: Context, address: String) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_STORE_ADDRESS, address) }
    }

    fun getStoreAddress(ctx: Context): String {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_STORE_ADDRESS, DEFAULT_STORE_ADDRESS) ?: DEFAULT_STORE_ADDRESS
    }

    fun saveStorePhone(ctx: Context, phone: String) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_STORE_PHONE, phone) }
    }

    fun getStorePhone(ctx: Context): String {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_STORE_PHONE, DEFAULT_STORE_PHONE) ?: DEFAULT_STORE_PHONE
    }

    fun savePaperWidth(ctx: Context, widthMode: Int) {
        require(
            widthMode == PAPER_58_NATIVE ||
                    widthMode == PAPER_80_NATIVE ||
                    widthMode == PAPER_58_ON_80
        ) {
            "Paper width mode must be 58, 80, or 5880"
        }
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { putInt(KEY_PAPER_WIDTH, widthMode) }
    }

    fun getPaperWidth(ctx: Context): Int {
        val width = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_PAPER_WIDTH, DEFAULT_PAPER_WIDTH)
        return when (width) {
            PAPER_80_NATIVE -> PAPER_80_NATIVE
            PAPER_58_ON_80 -> PAPER_58_ON_80
            else -> PAPER_58_NATIVE
        }
    }

    // =====================
    // CLEAR
    // =====================
    fun clearCustomerPrinter(ctx: Context) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { remove(KEY_CUSTOMER_MAC) }
    }

    fun clearDapurPrinter(ctx: Context) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { remove(KEY_DAPUR_MAC) }
    }

    fun clearMode(ctx: Context) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { remove(KEY_PRINTER_MODE) }
    }

    fun clearAll(ctx: Context) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { clear() }
    }
}
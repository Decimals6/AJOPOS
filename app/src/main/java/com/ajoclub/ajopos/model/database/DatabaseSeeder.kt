package com.ajoclub.ajopos.model.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {

    /**
     * Mengisi dummy data Kategori dan Produk ke dalam database.
     * Menggunakan CONFLICT_IGNORE agar aman bila diklik berulang kali
     * dan tidak menimpa data yang sudah ada.
     */
    suspend fun seedDummyData(db: AppDatabase): Result<String> = withContext(Dispatchers.IO) {
        try {
            val writableDb = db.openHelper.writableDatabase
            val now = System.currentTimeMillis()

            writableDb.beginTransaction()
            try {
                // 1. Seed Kategori (ID dikunci 101-103 agar relasi kategoriId di Produk selalu akurat)
                val kategoriList = listOf(
                    Triple(101, "Makanan", "Menu makanan utama dan berat"),
                    Triple(102, "Minuman", "Aneka kopi, teh, dan minuman segar"),
                    Triple(103, "Camilan", "Snack ringan pendamping")
                )

                for ((id, nama, ket) in kategoriList) {
                    val cv = ContentValues().apply {
                        put("id", id)
                        put("nama", nama)
                        put("keterangan", ket)
                        put("created_at", now)
                        put("updated_at", now)
                        put("deleted", 0)
                    }
                    // Gunakan SupportSQLiteDatabase.insert(table, conflictAlgorithm, values)
                    writableDb.insert("Kategori", SQLiteDatabase.CONFLICT_IGNORE, cv)
                }

                // 2. Seed Produk (ID dikunci 1001-1010)
                data class DummyProduk(
                    val id: Int,
                    val kategoriId: Int,
                    val nama: String,
                    val barcode: String?,
                    val harga: Double,
                    val keterangan: String
                )

                val produkList = listOf(
                    DummyProduk(1001, 101, "Nasi Goreng Spesial", "8990001001", 22000.0, "Telur mata sapi + ayam suwir"),
                    DummyProduk(1002, 101, "Mie Goreng Jawa", "8990001002", 20000.0, "Bumbu rempah khas"),
                    DummyProduk(1003, 101, "Ayam Geprek Sambal Bawang", "8990001003", 18000.0, "Nasi putih + lalapan"),
                    DummyProduk(1004, 101, "Rice Bowl Chicken Teriyaki", "8990001004", 25000.0, "Saus teriyaki wijen"),
                    DummyProduk(1005, 102, "Es Kopi Susu Gula Aren", "8990002001", 18000.0, "Espresso + susu segar + aren"),
                    DummyProduk(1006, 102, "Es Teh Manis Jumbo", "8990002002", 6000.0, "Teh melati seduh"),
                    DummyProduk(1007, 102, "Americano Ice", "8990002003", 15000.0, "Double shot arabica"),
                    DummyProduk(1008, 102, "Air Mineral 600ml", "8990002004", 5000.0, "Botol dingin"),
                    DummyProduk(1009, 103, "Kentang Goreng Original", "8990003001", 14000.0, "Disajikan dengan saus sambal & mayo"),
                    DummyProduk(1010, 103, "Roti Bakar Cokelat Keju", "8990003002", 16000.0, "Roti tebal panggang mentega")
                )

                for (p in produkList) {
                    val cv = ContentValues().apply {
                        put("id", p.id)
                        put("kategoriId", p.kategoriId)
                        put("nama", p.nama)
                        if (p.barcode != null) put("barcode", p.barcode) else putNull("barcode")
                        putNull("gambar")
                        put("harga", p.harga)
                        put("keterangan", p.keterangan)
                        put("created_at", now)
                        put("updated_at", now)
                        put("deleted", 0)
                    }
                    writableDb.insert("Produk", SQLiteDatabase.CONFLICT_IGNORE, cv)
                }

                writableDb.setTransactionSuccessful()
            } finally {
                writableDb.endTransaction()
            }

            Result.success("Berhasil mengisi 3 kategori dan 10 produk contoh")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
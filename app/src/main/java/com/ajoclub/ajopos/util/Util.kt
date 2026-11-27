package com.ajoclub.ajopos.util

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Contoh penambahan kolom:
        // database.execSQL("ALTER TABLE Produk ADD COLUMN stok_minimum INTEGER NOT NULL DEFAULT 0")

        // Contoh penambahan tabel:
        // database.execSQL("""
        //    CREATE TABLE IF NOT EXISTS ShiftLog(
        //        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        //        mulai LONG NOT NULL,
        //        selesai LONG NOT NULL
        //    )
        // """.trimIndent())

        // Kosong dulu, karena belum ada perubahan
    }
}

// MIGRATION_10_11
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Kamu tinggal isi kalau nanti versi 10 ada modifikasi
    }
}

// MIGRATION_11_12
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // dst...
    }
}
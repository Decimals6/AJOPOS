package com.ajoclub.ajopos.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ajoclub.ajopos.model.data.*
import com.ajoclub.ajopos.model.dao.*

@Database(
    entities = [
        User::class,
        Kategori::class,
        Produk::class,
        Nota::class,
        DetailNota::class,
        PrintJob::class
    ],
    version = AppDatabase.DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun kategoriDao(): KategoriDao
    abstract fun produkDao(): ProdukDao
    abstract fun notaDao(): NotaDao
    abstract fun detailNotaDao(): DetailNotaDao
    abstract fun printJobDao(): PrintJobDao

    /**
     * Memaksa SQLite memindahkan log sementara (WAL) ke file utama .db
     */
    fun checkpointWal() {
        if (isOpen) {
            openHelper.writableDatabase.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)")).use { cursor ->
                if (cursor.moveToFirst()) {
                    // Berhasil checkpoint
                }
            }
        }
    }

    companion object {
        const val DATABASE_NAME = "ajopos_database"
        const val DATABASE_VERSION = 10

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration()
//                    .addMigrations(
//                        MIGRATION_9_10,
//                        MIGRATION_10_11,
//                        MIGRATION_11_12
//                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Tutup koneksi dan lepaskan instance singleton sebelum file .db ditimpa
         */
        fun closeAndResetInstance() {
            synchronized(this) {
                if (INSTANCE?.isOpen == true) {
                    INSTANCE?.close()
                }
                INSTANCE = null
            }
        }
    }
}
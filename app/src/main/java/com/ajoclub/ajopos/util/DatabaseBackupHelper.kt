package com.ajoclub.ajopos.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.ajoclub.ajopos.model.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class DatabaseBackupHelper(private val context: Context) {

    private val dbFile: File
        get() = context.getDatabasePath(AppDatabase.DATABASE_NAME)

    /**
     * Salin database aktif ke target Uri (SAF Picker)
     */
    suspend fun exportDatabase(targetUri: Uri): BackupRestoreResult = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            db.checkpointWal()

            if (!dbFile.exists()) {
                return@withContext BackupRestoreResult.Error("Database lokal tidak ditemukan.")
            }

            context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                FileInputStream(dbFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext BackupRestoreResult.Error("Gagal membuka lokasi penyimpanan tujuan.")

            BackupRestoreResult.Success
        } catch (e: Exception) {
            BackupRestoreResult.Error("Gagal export database: ${e.localizedMessage ?: "Terjadi kesalahan"}")
        }
    }

    /**
     * Validasi dan timpa file database lokal dengan file backup
     */
    suspend fun importDatabase(sourceUri: Uri): BackupRestoreResult = withContext(Dispatchers.IO) {
        val tempCheckFile = File(context.cacheDir, "temp_restore.db")

        try {
            // 1. Salin ke cache internal untuk dicek dulu
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(tempCheckFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext BackupRestoreResult.Error("Gagal membaca file yang dipilih.")

            // 2. Baca SQLite user_version file backup
            val fileDbVersion = getDatabaseVersion(tempCheckFile)
            val currentAppDbVersion = AppDatabase.DATABASE_VERSION

            if (fileDbVersion > currentAppDbVersion) {
                tempCheckFile.delete()
                return@withContext BackupRestoreResult.VersionMismatch(
                    fileVersion = fileDbVersion,
                    appVersion = currentAppDbVersion
                )
            }

            // 3. Tutup koneksi Room aktif
            AppDatabase.closeAndResetInstance()

            // 4. Bersihkan file lama dan sisa log WAL/SHM
            val walFile = File(dbFile.parent, "${AppDatabase.DATABASE_NAME}-wal")
            val shmFile = File(dbFile.parent, "${AppDatabase.DATABASE_NAME}-shm")

            if (dbFile.exists()) dbFile.delete()
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            // 5. Timpa database dengan file baru
            tempCheckFile.copyTo(dbFile, overwrite = true)
            tempCheckFile.delete()

            // 6. Tes inisialisasi ulang database
            AppDatabase.getDatabase(context).openHelper.writableDatabase

            BackupRestoreResult.Success
        } catch (e: Exception) {
            tempCheckFile.delete()
            BackupRestoreResult.Error("Gagal memulihkan database: ${e.localizedMessage ?: "File rusak atau bukan SQLite"}")
        }
    }

    private fun getDatabaseVersion(sqliteFile: File): Int {
        var sqliteDb: SQLiteDatabase? = null
        return try {
            sqliteDb = SQLiteDatabase.openDatabase(
                sqliteFile.path,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            sqliteDb.version
        } finally {
            sqliteDb?.close()
        }
    }
}
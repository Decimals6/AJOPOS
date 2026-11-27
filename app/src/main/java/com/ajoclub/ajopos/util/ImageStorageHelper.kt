package com.ajoclub.ajopos.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {

    fun saveImageToInternal(context: Context, uri: Uri): String? {
        return try {
            val dir = File(context.filesDir, "produk_images")
            if (!dir.exists()) dir.mkdirs()

            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val destFile = File(dir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            fileName
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getImageFile(context: Context, fileName: String?): File? {
        if (fileName.isNullOrEmpty()) return null
        val file = File(File(context.filesDir, "produk_images"), fileName)
        return if (file.exists()) file else null
    }
}
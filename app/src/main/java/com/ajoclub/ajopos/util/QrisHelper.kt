package com.ajoclub.ajopos.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

object QrisHelper {

    private const val PREF_NAME = "qris_settings_pref"
    private const val KEY_QRIS_STRING = "qris_static_payload"

    fun saveStaticQris(context: Context, payload: String) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_QRIS_STRING, payload.trim()).apply()
    }

    fun getStaticQris(context: Context): String? {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sp.getString(KEY_QRIS_STRING, null)
    }

    /**
     * Konversi QRIS Statis ke Dinamis dengan menambahkan Tag 54 (Nominal)
     */
    fun createDynamicQris(rawQris: String, nominal: Long): String {
        var qris = rawQris.trim()

        // 1. Buang CRC lama di belakang (Tag 6304XXXX)
        val crcIndex = qris.lastIndexOf("6304")
        if (crcIndex != -1) {
            qris = qris.substring(0, crcIndex)
        }

        // 2. Ubah indikator statis (010211) menjadi dinamis (010212)
        qris = qris.replace("010211", "010212")

        // 3. Format Tag 54 (Transaction Amount): 54 + length(2 digit) + nominal
        val nominalStr = nominal.toString()
        val len = String.format("%02d", nominalStr.length)
        val tag54 = "54$len$nominalStr"

        // 4. Sisipkan Tag 54 tepat sebelum Tag 58 (Country code)
        val idx58 = qris.indexOf("5802")
        qris = if (idx58 != -1) {
            qris.substring(0, idx58) + tag54 + qris.substring(idx58)
        } else {
            qris + tag54
        }

        // 5. Tambahkan identifier CRC16 (Tag 63 length 04)
        qris += "6304"

        // 6. Hitung Checksum CRC16-CCITT
        val crc = crc16Ccitt(qris.toByteArray(Charsets.ISO_8859_1))
        return qris + crc
    }

    private fun crc16Ccitt(bytes: ByteArray): String {
        var crc = 0xFFFF
        val polynomial = 0x1021

        for (b in bytes) {
            for (i in 0 until 8) {
                val bit = (b.toInt() shr (7 - i) and 1) == 1
                val c15 = (crc shr 15 and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) crc = crc xor polynomial
            }
        }
        crc = crc and 0xFFFF
        return String.format("%04X", crc)
    }

    fun generateQrBitmap(content: String, size: Int = 600): Bitmap {
        val bitMatrix = MultiFormatWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size
        )
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
//package com.example.petungpos.bt
//
//import android.annotation.SuppressLint
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothSocket
//import android.util.Log
//import java.io.IOException
//import java.util.UUID
//
//object BluetoothPrinterManager {
//
//    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//
//    @SuppressLint("MissingPermission")
//    fun connect(mac: String): BluetoothSocket? {
//        return try {
//            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return null
//            if (!adapter.isEnabled) return null
//
//            val device = adapter.getRemoteDevice(mac)
//            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
//            adapter.cancelDiscovery()
//
//            try {
//                socket.connect()  // percobaan pertama
//            } catch (e: Exception) {
//                Log.e("BT_PRINTER", "Connect gagal pertama, retry... (${e.message})")
//                Thread.sleep(300) // napasin dulu bluetoothnya
//                try {
//                    socket.connect()   // retry kedua
//                } catch (ex: Exception) {
//                    Log.e("BT_PRINTER", "Gagal connect setelah retry: ${ex.message}")
//                    return null
//                }
//            }
//
//            socket
//        } catch (e: Exception) {
//            Log.e("BT_PRINTER", "Gagal connect: ${e.message}")
//            null
//        }
//    }
//
//
//    /**
//     * REVISI: Mengirim data dengan teknik CHUNKING (Dicicil)
//     * Supaya buffer printer murah tidak overflow.
//     */
//    fun print(socket: BluetoothSocket, data: ByteArray): Boolean {
//        return try {
//            val out = socket.outputStream
//
//            // Kirim per 1024 bytes (1KB)
//            val chunkSize = 1024
//            var offset = 0
//
//            while (offset < data.size) {
//                val remaining = data.size - offset
//                val count = if (remaining < chunkSize) remaining else chunkSize
//
//                // Tulis potongan data
//                out.write(data, offset, count)
//                out.flush() // Dorong data keluar
//
//                offset += count
//
//                // Jeda imut 100ms setiap kirim potongan biar printer nafas
//                try { Thread.sleep(100) } catch (e: Exception) {}
//            }
//
//            true
//        } catch (e: IOException) {
//            Log.e("BT_PRINTER", "Gagal kirim data: ${e.message}", e)
//            false
//        }
//    }
//
//
//
//    fun close(socket: BluetoothSocket?) {
//        try {
//            socket?.close()
//            Thread.sleep(300) // tambahin delay biar bluetooth state stabil
//        } catch (_: Exception) {}
//    }
//
//}

package com.ajoclub.ajopos.bt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.util.*

object BluetoothPrinterManager {
    private const val TAG = "BT_PRINTER"

    // UUID SPP
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    fun connect(mac: String): BluetoothSocket? {
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter == null) {
                Log.e(TAG, "Adapter null")
                return null
            }
            if (!adapter.isEnabled) {
                Log.e(TAG, "Bluetooth off")
                return null
            }
            val device = adapter.getRemoteDevice(mac)
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            adapter.cancelDiscovery()
            socket.connect()
            Log.d(TAG, "Connected to $mac")
            socket
        } catch (e: Exception) {
            Log.e(TAG, "Connect gagal ke $mac: ${e.message}", e)
            null
        }
    }

    /**
     * Print dengan chunking (agar tidak melebihi buffer printer).
     * Return true kalau semua bytes berhasil di-write.
     * Harus dipanggil di background thread.
     */
    fun print(socket: BluetoothSocket, data: ByteArray, chunkSize: Int = 1024): Boolean {
        return try {
            val out = socket.outputStream
            var offset = 0
            while (offset < data.size) {
                val len = minOf(chunkSize, data.size - offset)
                out.write(data, offset, len)
                out.flush()
                offset += len
                // opsional small pause tiap chunk agar printer sempat proses
                Thread.sleep(40)
            }
            Log.d(TAG, "Printed bytes=${data.size}")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Gagal kirim data: ${e.message}", e)
            false
        }
    }

    fun close(socket: BluetoothSocket?) {
        try {
            socket?.close()
            Log.d(TAG, "Socket closed")
        } catch (e: IOException) {
            Log.e(TAG, "Close socket error: ${e.message}", e)
        }
    }
}
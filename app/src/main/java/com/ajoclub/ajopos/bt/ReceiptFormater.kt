package com.ajoclub.ajopos.bt

import android.content.Context
import android.util.Log
import com.ajoclub.ajopos.model.data.DetailNotaDisplay
import com.ajoclub.ajopos.model.data.Nota
import com.ajoclub.ajopos.util.PrinterPrefs
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptFormatter {

    // === COMMANDS ESC/POS ===
    private val ESC_INIT = byteArrayOf(0x1B, 0x40)
    private val TEXT_ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
    private val TEXT_ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
    private val TEXT_ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02)
    private val TEXT_BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
    private val TEXT_BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
    private val FEED_PAPER = byteArrayOf(0x0A, 0x0A, 0x0A, 0x0A)

    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("in", "ID"))

    fun getLineWidth(paperMode: Int): Int {
        return when (paperMode) {
            PrinterPrefs.PAPER_80_NATIVE -> 48
            PrinterPrefs.PAPER_58_ON_80 -> 30
            else -> 32 // PAPER_58_NATIVE
        }
    }

    /**
     * Offset spasi ke kanan khusus untuk mode 58mm di printer 80mm (pakai pangkon/sekat).
     */
    private fun getLeftOffset(paperMode: Int): String {
        return when (paperMode) {
            PrinterPrefs.PAPER_58_ON_80 -> "         " // 9 spasi
            else -> "" // 0 spasi untuk 58mm murni maupun 80mm full
        }
    }

    // ---------------------
    // Helper functions
    // ---------------------
    private fun String.toPrinterBytes(): ByteArray {
        return try {
            this.toByteArray(charset("ISO-8859-1"))
        } catch (e: Exception) {
            this.toByteArray()
        }
    }

    private fun cleanText(input: String?): String {
        if (input.isNullOrBlank()) return ""
        return input.replace(Regex("[^\\p{L}0-9 \\-\\.,:()/%]"), "").trim()
    }

    private fun formatHarga(value: Double): String {
        return try {
            String.format(Locale.US, "%,.0f", value).replace(",", ".")
        } catch (e: Exception) {
            "0"
        }
    }

    private fun padRightSafe(text: String, len: Int): String {
        val s = cleanText(text)
        return if (s.length >= len) s.take(len) else s + " ".repeat(len - s.length)
    }

    private fun lineWithRight(leftPart: String, rightPart: String, lineWidth: Int): String {
        val left = cleanText(leftPart)
        val right = cleanText(rightPart)
        val spaceCount = lineWidth - left.length - right.length
        return if (spaceCount >= 1) {
            left + " ".repeat(spaceCount) + right
        } else {
            val maxLeftLen = (lineWidth - right.length - 1).coerceAtLeast(0)
            val truncated = if (left.length > maxLeftLen) left.take(maxLeftLen) else left
            "$truncated $right"
        }
    }

    // ---------------------
    // Customer receipt (ESC/POS)
    // ---------------------
    fun buildCustomerReceipt(
        ctx: Context,
        nota: Nota,
        items: List<DetailNotaDisplay>,
        cash: String? = null
    ): ByteArray {
        val bos = ByteArrayOutputStream()
        try {
            val paperMode = PrinterPrefs.getPaperWidth(ctx)
            val lineWidth = getLineWidth(paperMode)
            val offset = getLeftOffset(paperMode)
            val dividerLine = offset + "-".repeat(lineWidth) + "\n"

            val idNota = nota.localId.removePrefix("nota-")
            val tgl = dateFormat.format(Date(nota.waktu))
            val pelanggan = if (nota.pelanggan.isNullOrBlank()) "UMUM" else cleanText(nota.pelanggan)

            val storeName = PrinterPrefs.getStoreName(ctx).ifBlank { "AJOCLUB" }
            val storeAddress = PrinterPrefs.getStoreAddress(ctx).ifBlank { "Jl. Intan 3 no 42\nKec. Paras, Mulung" }
            val storePhone = PrinterPrefs.getStorePhone(ctx).ifBlank { "+12345678910" }

            bos.write(ESC_INIT)

            // Header (center)
            bos.write(TEXT_ALIGN_CENTER)
            bos.write(TEXT_BOLD_ON)
            bos.write((cleanText(storeName) + "\n").toPrinterBytes())
            bos.write(TEXT_BOLD_OFF)

            storeAddress.lines()
                .map { cleanText(it) }
                .filter { it.isNotBlank() }
                .forEach { addressLine ->
                    bos.write((addressLine + "\n").toPrinterBytes())
                }

            bos.write((cleanText(storePhone) + "\n").toPrinterBytes())
            bos.write(dividerLine.toPrinterBytes())

            // Info (left)
            bos.write(TEXT_ALIGN_LEFT)
            bos.write((offset + "Ref : $idNota\n").toPrinterBytes())
            bos.write((offset + "Tgl : $tgl\n").toPrinterBytes())
            bos.write((offset + "Plg : $pelanggan\n").toPrinterBytes())
            bos.write(dividerLine.toPrinterBytes())

            // Items header
            bos.write(TEXT_ALIGN_CENTER)
            bos.write("${nota.tipe}\n".toPrinterBytes())
            bos.write(TEXT_ALIGN_LEFT)
            bos.write((offset + "Item\n").toPrinterBytes())
            bos.write(dividerLine.toPrinterBytes())

            if (items.isEmpty()) {
                bos.write((offset + "[DATA ITEM KOSONG]\n").toPrinterBytes())
            } else {
                for (item in items) {
                    try {
                        val nameLine = padRightSafe(item.produkNama, lineWidth)
                        bos.write((offset + nameLine + "\n").toPrinterBytes())

                        val desc = item.keterangan?.trim()
                        if (!desc.isNullOrEmpty()) {
                            bos.write((offset + "Ket: $desc\n").toPrinterBytes())
                        }

                        val price = formatHarga(item.harga)
                        val qty = item.jumlah.toString()
                        val left = "   $price x$qty"
                        val right = "Rp ${formatHarga(item.subtotal)}"
                        bos.write((offset + lineWithRight(left, right, lineWidth) + "\n").toPrinterBytes())
                        bos.write("\n".toPrinterBytes())
                    } catch (e: Exception) {
                        Log.e("ReceiptFormatter", "Error printing item: ${e.message}")
                    }
                }
            }

            bos.write(dividerLine.toPrinterBytes())

            // Total
            val totalStr = "Rp ${formatHarga(nota.total)}"
            bos.write(TEXT_BOLD_ON)
            bos.write(TEXT_ALIGN_LEFT)
            bos.write((offset + lineWithRight("Total :", totalStr, lineWidth)).toPrinterBytes())
            bos.write(TEXT_BOLD_OFF)
            bos.write("\n".toPrinterBytes())

            // Payment
            if (cash != null) {
                bos.write((offset + lineWithRight("Pembayaran :", "CASH", lineWidth)).toPrinterBytes())
                val totalInt = nota.total.toInt()
                val cashInt = cash.filter { it.isDigit() }.toIntOrNull() ?: 0
                val kembalian = cashInt - totalInt

                bos.write("\n".toPrinterBytes())
                bos.write((offset + lineWithRight("Dibayar :", formatHarga(cashInt.toDouble()), lineWidth)).toPrinterBytes())
                bos.write((offset + lineWithRight("Kembalian :", formatHarga(kembalian.toDouble()), lineWidth)).toPrinterBytes())
            } else {
                bos.write((offset + lineWithRight("Pembayaran :", "QRIS", lineWidth)).toPrinterBytes())
            }

            bos.write("\n".toPrinterBytes())

            // Footer
            bos.write(TEXT_ALIGN_CENTER)
            bos.write("\nTerima Kasih\n".toPrinterBytes())
            bos.write("Semoga hari Anda menyenangkan\n".toPrinterBytes())
            bos.write(FEED_PAPER)

        } catch (e: Exception) {
            Log.e("ReceiptFormatter", "Global Error: ${e.message}")
        }
        return bos.toByteArray()
    }

    // ---------------------
    // Kitchen receipt (ESC/POS)
    // ---------------------
    fun buildKitchenReceipt(
        ctx: Context,
        nota: Nota,
        items: List<DetailNotaDisplay>
    ): ByteArray {
        val bos = ByteArrayOutputStream()
        try {
            val paperMode = PrinterPrefs.getPaperWidth(ctx)
            val lineWidth = getLineWidth(paperMode)
            val offset = getLeftOffset(paperMode)
            val dividerLine = offset + "-".repeat(lineWidth) + "\n"

            val idNota = nota.localId.removePrefix("nota-")
            val tgl = dateFormat.format(Date(nota.waktu))

            bos.write(ESC_INIT)
            bos.write(TEXT_ALIGN_CENTER)
            bos.write(TEXT_BOLD_ON)
            bos.write("KITCHEN ORDER\n".toPrinterBytes())
            bos.write(TEXT_BOLD_OFF)
            bos.write(dividerLine.toPrinterBytes())

            bos.write(TEXT_ALIGN_LEFT)
            bos.write((offset + "Ref : $idNota\n").toPrinterBytes())
            bos.write((offset + "Tgl : $tgl\n").toPrinterBytes())
            if (!nota.pelanggan.isNullOrBlank()) {
                bos.write((offset + "Plg : ${cleanText(nota.pelanggan)}\n").toPrinterBytes())
            }
            bos.write(dividerLine.toPrinterBytes())
            bos.write(TEXT_ALIGN_CENTER)
            bos.write("${nota.tipe}\n".toPrinterBytes())
            bos.write(TEXT_ALIGN_LEFT)
            bos.write((offset + "Item\n").toPrinterBytes())
            bos.write(dividerLine.toPrinterBytes())

            for (item in items) {
                try {
                    val line = "${item.jumlah} x ${cleanText(item.produkNama)}\n"
                    bos.write(TEXT_BOLD_ON)
                    bos.write((offset + line).toPrinterBytes())
                    bos.write(TEXT_BOLD_OFF)
                    if (!item.keterangan.isNullOrBlank()) {
                        bos.write((offset + "  Ket: ${cleanText(item.keterangan)}\n").toPrinterBytes())
                    }
                    bos.write("\n".toPrinterBytes())
                } catch (_: Exception) {
                }
            }

            bos.write(dividerLine.toPrinterBytes())
            bos.write(FEED_PAPER)
        } catch (e: Exception) {
            Log.e("ReceiptFormatter", "Kitchen Error: ${e.message}")
        }
        return bos.toByteArray()
    }

    // ---------------------
    // Test receipt
    // ---------------------
    fun buildTestReceipt(ctx: Context): ByteArray {
        val bos = ByteArrayOutputStream()
        try {
            val paperMode = PrinterPrefs.getPaperWidth(ctx)
            val lineWidth = getLineWidth(paperMode)
            val offset = getLeftOffset(paperMode)
            val dividerLine = offset + "-".repeat(lineWidth) + "\n"
            val labelMode = if (paperMode == PrinterPrefs.PAPER_58_ON_80) "58mm on 80mm" else "${paperMode}mm"

            bos.write(ESC_INIT)
            bos.write(TEXT_ALIGN_CENTER)
            bos.write(TEXT_BOLD_ON)
            bos.write("TEST PRINTER OK ($labelMode)\n".toPrinterBytes())
            bos.write(TEXT_BOLD_OFF)
            bos.write(dividerLine.toPrinterBytes())
            bos.write(TEXT_ALIGN_LEFT)
            bos.write((offset + "Charset: ISO-8859-1\n").toPrinterBytes())
            bos.write(FEED_PAPER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bos.toByteArray()
    }

    // ---------------------
    // Plain Text Generator for UI Preview
    // ---------------------
    fun generateDummyNota(): Pair<Nota, List<DetailNotaDisplay>> {
        val now = System.currentTimeMillis()

        val dummyNota = Nota(
            localId = "nota-SAMPLE-001",
            serverId = null,
            userId = 1,
            pelanggan = "Budi Santoso",
            waktu = now,
            tipe = "DINE IN",
            total = 45000.0,
            pembayaran = "CASH",
            sync = 0,
            created_at = now,
            updated_at = now,
            deleted = 0
        )

        val dummyItems = listOf(
            DetailNotaDisplay(
                id = 1,
                produkId = 101,
                produkNama = "Kopi Susu Gula Aren",
                keterangan = "Less ice",
                harga = 18000.0,
                jumlah = 2,
                subtotal = 36000.0
            ),
            DetailNotaDisplay(
                id = 2,
                produkId = 102,
                produkNama = "Roti Bakar Cokelat",
                keterangan = null,
                harga = 9000.0,
                jumlah = 1,
                subtotal = 9000.0
            )
        )

        return Pair(dummyNota, dummyItems)
    }

    fun buildReceiptPlainText(
        ctx: Context,
        nota: Nota,
        items: List<DetailNotaDisplay>,
        cash: String? = "50000",
        targetPaperWidth: Int? = null,
        customStoreName: String? = null,
        customStoreAddress: String? = null,
        customStorePhone: String? = null
    ): String {
        val sb = StringBuilder()
        val paperMode = targetPaperWidth ?: PrinterPrefs.getPaperWidth(ctx)
        // Di UI preview layar tidak perlu offset spasi fisik agar tetap proporsional
        val lineWidth = getLineWidth(paperMode)

        val idNota = nota.localId.removePrefix("nota-")
        val tgl = dateFormat.format(Date(nota.waktu))
        val pelanggan = if (nota.pelanggan.isNullOrBlank()) "UMUM" else cleanText(nota.pelanggan)

        val storeName = (customStoreName ?: PrinterPrefs.getStoreName(ctx)).ifBlank { "AJOCLUB" }
        val storeAddress = (customStoreAddress ?: PrinterPrefs.getStoreAddress(ctx)).ifBlank { "Jl. Intan 3 no 42\nKec. Paras, Mulung" }
        val storePhone = (customStorePhone ?: PrinterPrefs.getStorePhone(ctx)).ifBlank { "+12345678910" }

        fun center(text: String): String {
            val s = cleanText(text)
            if (s.length >= lineWidth) return s.take(lineWidth)
            val left = (lineWidth - s.length) / 2
            return " ".repeat(left) + s
        }

        val divider = "-".repeat(lineWidth)

        sb.appendLine(center(storeName))
        storeAddress.lines().filter { it.isNotBlank() }.forEach {
            sb.appendLine(center(it))
        }
        sb.appendLine(center(storePhone))
        sb.appendLine(divider)

        sb.appendLine("Ref : $idNota")
        sb.appendLine("Tgl : $tgl")
        sb.appendLine("Plg : $pelanggan")
        sb.appendLine(divider)

        sb.appendLine(center(nota.tipe))
        sb.appendLine("Item")
        sb.appendLine(divider)

        for (item in items) {
            sb.appendLine(padRightSafe(item.produkNama, lineWidth))
            if (!item.keterangan.isNullOrBlank()) {
                sb.appendLine("Ket: ${item.keterangan?.trim()}")
            }
            val price = formatHarga(item.harga)
            val left = "   $price x${item.jumlah}"
            val right = "Rp ${formatHarga(item.subtotal)}"
            sb.appendLine(lineWithRight(left, right, lineWidth))
        }

        sb.appendLine(divider)
        sb.appendLine(lineWithRight("Total :", "Rp ${formatHarga(nota.total)}", lineWidth))
        sb.appendLine()

        if (cash != null) {
            sb.appendLine(lineWithRight("Pembayaran :", "CASH", lineWidth))
            val totalInt = nota.total.toInt()
            val cashInt = cash.filter { it.isDigit() }.toIntOrNull() ?: 0
            val kembalian = cashInt - totalInt
            sb.appendLine(lineWithRight("Dibayar :", formatHarga(cashInt.toDouble()), lineWidth))
            sb.appendLine(lineWithRight("Kembalian :", formatHarga(kembalian.toDouble()), lineWidth))
        } else {
            sb.appendLine(lineWithRight("Pembayaran :", "QRIS", lineWidth))
        }

        sb.appendLine()
        sb.appendLine(center("Terima Kasih"))
        sb.appendLine(center("Semoga hari Anda menyenangkan"))

        return sb.toString()
    }
}
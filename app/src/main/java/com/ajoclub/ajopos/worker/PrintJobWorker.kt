package com.ajoclub.ajopos.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ajoclub.ajopos.bt.BluetoothPrinterManager
import com.ajoclub.ajopos.model.data.PrintJobStatus
import com.ajoclub.ajopos.model.database.AppDatabase
import com.ajoclub.ajopos.util.PrinterPrefs
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream

class PrintJobWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val TAG = "WORKER_PRINT"

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val db = AppDatabase.getDatabase(ctx)

        val mode = PrinterPrefs.getPrinterMode(ctx)
        if (mode == "none") return Result.success()

        val customerMac = PrinterPrefs.getCustomerPrinter(ctx)
        val kitchenMac = PrinterPrefs.getDapurPrinter(ctx)

        // Ambil mode kertas saat ini (58, 80, atau 5880)
        val paperMode = PrinterPrefs.getPaperWidth(ctx)

        val jobs = db.printJobDao().getPending(limit = 20)
        if (jobs.isEmpty()) {
            Log.d(TAG, "No pending print jobs")
            return Result.success()
        }

        val map = mutableMapOf<String, MutableList<com.ajoclub.ajopos.model.data.PrintJob>>()
        for (job in jobs) {
            val mac = when (job.target) {
                "CUSTOMER" -> customerMac
                "KITCHEN" -> kitchenMac
                else -> null
            }
            if (mac.isNullOrBlank()) {
                updateJob(db, job, PrintJobStatus.FAILED, "No configured printer for ${job.target}")
                continue
            }
            map.getOrPut(mac) { mutableListOf() }.add(job)
        }

        for ((mac, listJobs) in map) {
            Log.d(TAG, "Processing ${listJobs.size} job(s) for printer $mac")
            val socket = BluetoothPrinterManager.connect(mac)
            if (socket == null) {
                for (job in listJobs) {
                    updateJob(db, job, PrintJobStatus.FAILED, "Connect failed to $mac")
                }
                continue
            }

            for (job in listJobs) {
                // Oper paperMode ke preparePayloadWithCutter
                val finalPayload = preparePayloadWithCutter(job.payload, paperMode)

                val printed = BluetoothPrinterManager.print(socket, finalPayload)
                if (printed) {
                    delay(1600)
                    updateJob(db, job, PrintJobStatus.DONE, null)
                    Log.d(TAG, "Job ${job.id} DONE on $mac")
                } else {
                    updateJob(db, job, PrintJobStatus.FAILED, "Send failed")
                    Log.d(TAG, "Job ${job.id} FAILED on $mac")
                }
            }

            delay(1200)
            BluetoothPrinterManager.close(socket)
        }

        return Result.success()
    }

    /**
     * Mengatur jarak feed kertas sebelum command auto-cutter:
     * - 58mm murni: Tanpa tambahan feed ekstra (karena di ReceiptFormatter sudah ada FEED_PAPER)
     * - 80mm / 58mm di 80mm: Tambah 4 baris feed agar tulisan melewati bilah pisau cutter
     */
    private fun preparePayloadWithCutter(rawPayload: ByteArray, paperMode: Int): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(rawPayload)

        // Pastikan buffer teks ditutup dengan Line Feed (\n)
        out.write(byteArrayOf(0x0A))

        // Tambah feed ekstra hanya jika memakai printer 80mm (80 full maupun 58 pakai sekat)
        if (paperMode != PrinterPrefs.PAPER_58_NATIVE) {
            // ESC d 4 -> dorong kertas 4 baris ke depan sebelum potong
            out.write(byteArrayOf(0x1B, 0x64, 0x04))
        }

        // Command Auto Cutter (GS V 65 0)
        out.write(byteArrayOf(0x1D, 0x56, 0x41, 0x00))

        return out.toByteArray()
    }

    private suspend fun updateJob(
        db: AppDatabase,
        job: com.ajoclub.ajopos.model.data.PrintJob,
        status: String,
        error: String?
    ) {
        val updated = job.copy(
            status = status,
            errorMsg = error,
            attempts = job.attempts + 1,
            updated_at = System.currentTimeMillis()
        )
        db.printJobDao().update(updated)
    }

    companion object {
        fun enqueueNow(context: Context) {
            val appCtx = context.applicationContext
            val request = OneTimeWorkRequestBuilder<PrintJobWorker>().build()

            WorkManager.getInstance(appCtx)
                .enqueueUniqueWork(
                    "print_job_worker",
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    request
                )
        }
    }
}
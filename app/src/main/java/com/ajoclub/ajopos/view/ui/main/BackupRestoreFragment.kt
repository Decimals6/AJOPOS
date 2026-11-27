package com.ajoclub.ajopos.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ajoclub.ajopos.databinding.FragmentBackupRestoreBinding
import com.ajoclub.ajopos.util.BackupRestoreResult
import com.ajoclub.ajopos.util.DatabaseBackupHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupRestoreFragment : Fragment() {

    private var _binding: FragmentBackupRestoreBinding? = null
    private val binding get() = _binding!!

    private lateinit var backupHelper: DatabaseBackupHelper
    private var pendingRestoreUri: Uri? = null

    // File Picker untuk Create/Save File Backup
    private val createBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { executeBackup(it) }
    }

    // File Picker untuk Pilih File Backup (.db)
    private val openRestoreLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingRestoreUri = it
            showRestoreConfirmationDialog(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackupRestoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backupHelper = DatabaseBackupHelper(requireContext())

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBackup.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val defaultFileName = "AJOPOS_BACKUP_$timestamp.db"
            createBackupLauncher.launch(defaultFileName)
        }

        binding.btnRestore.setOnClickListener {
            // Membuka file picker untuk segala jenis binary file SQLite
            openRestoreLauncher.launch(arrayOf("*/*"))
        }
    }

    private fun executeBackup(targetUri: Uri) {
        setLoading(true)
        lifecycleScope.launch {
            when (val result = backupHelper.exportDatabase(targetUri)) {
                is BackupRestoreResult.Success -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Cadangan database berhasil disimpan!", Toast.LENGTH_LONG).show()
                }
                is BackupRestoreResult.Error -> {
                    setLoading(false)
                    showErrorDialog("Gagal Backup", result.message)
                }
                else -> setLoading(false)
            }
        }
    }

    private fun showRestoreConfirmationDialog(uri: Uri) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Timpa Data")
            .setMessage("PERINGATAN: Seluruh data saat ini akan dihapus dan digantikan oleh data dari file cadangan terpilih. Tindakan ini tidak dapat dibatalkan.\n\nLanjutkan pemulihan?")
            .setPositiveButton("Ya, Timpa Data") { _, _ ->
                executeRestore(uri)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun executeRestore(sourceUri: Uri) {
        setLoading(true)
        lifecycleScope.launch {
            when (val result = backupHelper.importDatabase(sourceUri)) {
                is BackupRestoreResult.Success -> {
                    setLoading(false)
                    showRestartAppDialog()
                }
                is BackupRestoreResult.VersionMismatch -> {
                    setLoading(false)
                    showErrorDialog(
                        "Versi Database Tidak Sesuai",
                        "File backup berasal dari database versi ${result.fileVersion}, sedangkan aplikasi saat ini masih versi ${result.appVersion}.\n\nSilakan perbarui aplikasi PCS POS ke versi yang lebih baru terlebih dahulu."
                    )
                }
                is BackupRestoreResult.Error -> {
                    setLoading(false)
                    showErrorDialog("Gagal Memulihkan Data", result.message)
                }
            }
        }
    }

    private fun showRestartAppDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Pemulihan Berhasil")
            .setMessage("Database berhasil dipulihkan. Aplikasi perlu dimuat ulang agar seluruh data baru sinkron secara sempurna.")
            .setCancelable(false)
            .setPositiveButton("Mulai Ulang Aplikasi") { _, _ ->
                restartApplication()
            }
            .show()
    }

    private fun restartApplication() {
        val packageManager = requireContext().packageManager
        val intent = packageManager.getLaunchIntentForPackage(requireContext().packageName)
        val componentName = intent?.component
        val restartIntent = Intent.makeRestartActivityTask(componentName)
        startActivity(restartIntent)
        Runtime.getRuntime().exit(0)
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnBackup.isEnabled = !isLoading
        binding.btnRestore.isEnabled = !isLoading
    }

    private fun showErrorDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Mengerti", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
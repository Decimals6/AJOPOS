package com.ajoclub.ajopos.util

sealed class BackupRestoreResult {
    object Success : BackupRestoreResult()
    data class Error(val message: String) : BackupRestoreResult()
    data class VersionMismatch(
        val fileVersion: Int,
        val appVersion: Int
    ) : BackupRestoreResult()
}
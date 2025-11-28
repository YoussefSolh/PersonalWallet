package com.youssefsolh.personalwallet.domain.repository

import com.youssefsolh.personalwallet.domain.model.BackupData

interface BackupRepository {
    fun setCurrentUser(email: String?)
    suspend fun createBackup(): Result<BackupData>
    suspend fun uploadBackupToDrive(backupData: BackupData): Result<String>
    suspend fun downloadBackupFromDrive(fileId: String): Result<BackupData>
    suspend fun restoreBackup(backupData: BackupData): Result<Unit>
    suspend fun getLastBackupTimestamp(): Long?
    suspend fun listDriveBackups(): Result<List<DriveBackupInfo>>
}

data class DriveBackupInfo(
    val id: String,
    val name: String,
    val timestamp: Long,
    val size: Long
)

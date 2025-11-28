package com.youssefsolh.personalwallet.data.remote.drive

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.youssefsolh.personalwallet.domain.model.BackupData
import com.youssefsolh.personalwallet.domain.repository.DriveBackupInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "DriveService"
    }

    private val json = Json { prettyPrint = true }

    private fun getDriveService(accountName: String): Drive {
        Log.d(TAG, "Creating Drive service for account: $accountName")

        if (accountName.isBlank()) {
            throw IllegalArgumentException("Account name cannot be blank")
        }

        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccountName = accountName

        Log.d(TAG, "Credential created with account: ${credential.selectedAccountName}")

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Personal Wallet")
            .build()
    }

    suspend fun uploadBackup(accountName: String, backupData: BackupData): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting backup upload for account: $accountName")

            if (accountName.isBlank()) {
                Log.e(TAG, "Account name is blank")
                return@withContext Result.failure(Exception("Account name cannot be empty"))
            }

            val drive = getDriveService(accountName)
            val jsonContent = json.encodeToString(backupData)
            Log.d(TAG, "Backup data serialized, size: ${jsonContent.length} bytes")

            val fileName = "wallet_backup_${System.currentTimeMillis()}.json"
            Log.d(TAG, "Creating file with name: $fileName")

            val fileMetadata = File()
            fileMetadata.setName(fileName)
            fileMetadata.setParents(listOf("appDataFolder"))

            Log.d(TAG, "File metadata created - name: ${fileMetadata.getName()}, parents: ${fileMetadata.getParents()}")

            val mediaContent = ByteArrayContent(
                "application/json",
                jsonContent.toByteArray()
            )

            Log.d(TAG, "Uploading file to Google Drive...")
            val file = drive.files().create(fileMetadata, mediaContent)
                .setFields("id, name")
                .execute()

            Log.d(TAG, "Backup uploaded successfully - file ID: ${file.id}, name: ${file.name}")
            Result.success(file.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload backup", e)
            Result.failure(Exception("Failed to upload backup: ${e.message}", e))
        }
    }

    suspend fun downloadBackup(accountName: String, fileId: String): Result<BackupData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Downloading backup file: $fileId")
            val drive = getDriveService(accountName)

            val outputStream = ByteArrayOutputStream()
            drive.files().get(fileId).executeMediaAndDownloadTo(outputStream)

            val jsonContent = outputStream.toString("UTF-8")
            Log.d(TAG, "Downloaded backup data, size: ${jsonContent.length} bytes")
            val backupData = json.decodeFromString<BackupData>(jsonContent)

            Log.d(TAG, "Backup download successful")
            Result.success(backupData)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download backup", e)
            Result.failure(e)
        }
    }

    suspend fun listBackups(accountName: String): Result<List<DriveBackupInfo>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Listing backups for account: $accountName")
            val drive = getDriveService(accountName)

            val result = drive.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name, createdTime, size)")
                .setQ("name contains 'wallet_backup'")
                .setOrderBy("createdTime desc")
                .execute()

            val backups = result.files.map { file ->
                DriveBackupInfo(
                    id = file.id,
                    name = file.name,
                    timestamp = file.createdTime?.value ?: 0L,
                    size = file.getSize() ?: 0L
                )
            }

            Log.d(TAG, "Found ${backups.size} backup(s)")
            Result.success(backups)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to list backups", e)
            Result.failure(e)
        }
    }

    suspend fun deleteBackup(accountName: String, fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting backup file: $fileId")
            val drive = getDriveService(accountName)
            drive.files().delete(fileId).execute()
            Log.d(TAG, "Backup deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete backup", e)
            Result.failure(e)
        }
    }
}

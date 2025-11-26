package com.youssefsolh.personalwallet.data.remote.drive

import android.content.Context
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { prettyPrint = true }

    private fun getDriveService(accountName: String): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccountName = accountName
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Personal Wallet")
            .build()
    }

    suspend fun uploadBackup(accountName: String, backupData: BackupData): Result<String> {
        return try {
            val drive = getDriveService(accountName)
            val jsonContent = json.encodeToString(backupData)

            val fileMetadata = File().apply {
                name = "wallet_backup_${System.currentTimeMillis()}.json"
                parents = listOf("appDataFolder")
            }

            val mediaContent = ByteArrayContent(
                "application/json",
                jsonContent.toByteArray()
            )

            val file = drive.files().create(fileMetadata, mediaContent)
                .setFields("id, name")
                .execute()

            Result.success(file.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadBackup(accountName: String, fileId: String): Result<BackupData> {
        return try {
            val drive = getDriveService(accountName)

            val outputStream = ByteArrayOutputStream()
            drive.files().get(fileId).executeMediaAndDownloadTo(outputStream)

            val jsonContent = outputStream.toString("UTF-8")
            val backupData = json.decodeFromString<BackupData>(jsonContent)

            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listBackups(accountName: String): Result<List<DriveBackupInfo>> {
        return try {
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

            Result.success(backups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBackup(accountName: String, fileId: String): Result<Unit> {
        return try {
            val drive = getDriveService(accountName)
            drive.files().delete(fileId).execute()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

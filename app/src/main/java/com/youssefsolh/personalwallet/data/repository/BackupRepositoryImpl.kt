package com.youssefsolh.personalwallet.data.repository

import android.util.Log
import com.youssefsolh.personalwallet.data.local.CurrentUserProvider
import com.youssefsolh.personalwallet.data.local.dao.CategoryDao
import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.data.local.dao.WalletDao
import com.youssefsolh.personalwallet.data.local.entity.toDomain
import com.youssefsolh.personalwallet.data.local.entity.toEntity
import com.youssefsolh.personalwallet.data.remote.auth.AuthService
import com.youssefsolh.personalwallet.data.remote.drive.DriveService
import com.youssefsolh.personalwallet.domain.model.BackupData
import com.youssefsolh.personalwallet.domain.repository.AuthRepository
import com.youssefsolh.personalwallet.domain.repository.BackupRepository
import com.youssefsolh.personalwallet.domain.repository.DriveBackupInfo
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val driveService: DriveService,
    private val authRepository: AuthRepository,
    private val authService: AuthService,
    private val currentUserProvider: CurrentUserProvider
) : BackupRepository {

    companion object {
        private const val TAG = "BackupRepository"
    }

    private var lastBackupTimestamp: Long? = null

    override fun setCurrentUser(email: String?) {
        // No longer needed - we get user from authRepository
    }

    private suspend fun getCurrentUserEmail(): String? {
        // Get the actual Google account from the device, not from cached User model
        val googleAccount = authService.getCurrentGoogleAccount()
        Log.d(TAG, "Getting current Google account - email: ${googleAccount?.email}")

        if (googleAccount == null) {
            Log.w(TAG, "No Google account signed in")
            return null
        }

        val email = googleAccount.email
        // Ensure email is not blank
        val result = if (email.isNullOrBlank()) null else email
        Log.d(TAG, "Current Google account email result: $result")
        return result
    }

    override suspend fun createBackup(): Result<BackupData> {
        return try {
            val userId = currentUserProvider.getCurrentUserId()
            val wallets = walletDao.getAllWalletsSync(userId).map { it.toDomain() }
            val transactions = transactionDao.getAllTransactions(userId).first().map { it.toDomain() }
            val categories = categoryDao.getAllCategories(userId).first().map { it.toDomain() }

            val backupData = BackupData(
                wallets = wallets,
                transactions = transactions,
                categories = categories
            )

            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadBackupToDrive(backupData: BackupData): Result<String> {
        Log.d(TAG, "uploadBackupToDrive called")
        val email = getCurrentUserEmail()
        if (email == null) {
            Log.e(TAG, "Cannot upload backup - user not signed in")
            return Result.failure(Exception("User not signed in. Please sign in with a Google account to use backup."))
        }

        Log.d(TAG, "Uploading backup for email: $email")
        return driveService.uploadBackup(email, backupData).onSuccess {
            lastBackupTimestamp = System.currentTimeMillis()
            Log.d(TAG, "Backup upload successful, timestamp updated")
        }
    }

    override suspend fun downloadBackupFromDrive(fileId: String): Result<BackupData> {
        val email = getCurrentUserEmail() ?: return Result.failure(Exception("User not signed in"))
        return driveService.downloadBackup(email, fileId)
    }

    override suspend fun restoreBackup(backupData: BackupData): Result<Unit> {
        return try {
            val userId = currentUserProvider.getCurrentUserId()

            // Clear existing data
            walletDao.deleteAllWallets()
            transactionDao.deleteAllTransactions()
            categoryDao.deleteAllCategories()

            // Insert backup data
            backupData.wallets.forEach { wallet ->
                walletDao.insertWallet(wallet.toEntity(userId))
            }

            backupData.transactions.forEach { transaction ->
                transactionDao.insertTransaction(transaction.toEntity(userId))
            }

            backupData.categories.forEach { category ->
                categoryDao.insertCategory(category.toEntity(userId))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLastBackupTimestamp(): Long? {
        return lastBackupTimestamp
    }

    override suspend fun listDriveBackups(): Result<List<DriveBackupInfo>> {
        val email = getCurrentUserEmail() ?: return Result.failure(Exception("User not signed in"))
        return driveService.listBackups(email)
    }
}

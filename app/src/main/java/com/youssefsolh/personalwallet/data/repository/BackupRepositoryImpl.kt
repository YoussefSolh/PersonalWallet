package com.youssefsolh.personalwallet.data.repository

import com.youssefsolh.personalwallet.data.local.dao.CategoryDao
import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.data.local.dao.WalletDao
import com.youssefsolh.personalwallet.data.local.entity.toDomain
import com.youssefsolh.personalwallet.data.local.entity.toEntity
import com.youssefsolh.personalwallet.data.remote.drive.DriveService
import com.youssefsolh.personalwallet.domain.model.BackupData
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
    private val driveService: DriveService
) : BackupRepository {

    private var lastBackupTimestamp: Long? = null
    private var currentUserEmail: String? = null

    fun setCurrentUser(email: String?) {
        currentUserEmail = email
    }

    override suspend fun createBackup(): Result<BackupData> {
        return try {
            val wallets = walletDao.getAllWalletsSync().map { it.toDomain() }
            val transactions = transactionDao.getAllTransactions().first().map { it.toDomain() }
            val categories = categoryDao.getAllCategories().first().map { it.toDomain() }

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
        val email = currentUserEmail ?: return Result.failure(Exception("User not signed in"))

        return driveService.uploadBackup(email, backupData).onSuccess {
            lastBackupTimestamp = System.currentTimeMillis()
        }
    }

    override suspend fun downloadBackupFromDrive(fileId: String): Result<BackupData> {
        val email = currentUserEmail ?: return Result.failure(Exception("User not signed in"))
        return driveService.downloadBackup(email, fileId)
    }

    override suspend fun restoreBackup(backupData: BackupData): Result<Unit> {
        return try {
            // Clear existing data
            walletDao.deleteAllWallets()
            transactionDao.deleteAllTransactions()
            categoryDao.deleteAllCategories()

            // Insert backup data
            backupData.wallets.forEach { wallet ->
                walletDao.insertWallet(wallet.toEntity())
            }

            backupData.transactions.forEach { transaction ->
                transactionDao.insertTransaction(transaction.toEntity())
            }

            backupData.categories.forEach { category ->
                categoryDao.insertCategory(category.toEntity())
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
        val email = currentUserEmail ?: return Result.failure(Exception("User not signed in"))
        return driveService.listBackups(email)
    }
}

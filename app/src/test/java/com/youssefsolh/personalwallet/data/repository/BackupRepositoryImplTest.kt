package com.youssefsolh.personalwallet.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.youssefsolh.personalwallet.data.local.dao.CategoryDao
import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.data.local.dao.WalletDao
import com.youssefsolh.personalwallet.data.local.entity.CategoryEntity
import com.youssefsolh.personalwallet.data.local.entity.TransactionEntity
import com.youssefsolh.personalwallet.data.local.entity.WalletEntity
import com.youssefsolh.personalwallet.data.remote.auth.AuthService
import com.youssefsolh.personalwallet.data.remote.drive.DriveService
import com.youssefsolh.personalwallet.domain.model.BackupData
import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.model.User
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.AuthRepository
import com.youssefsolh.personalwallet.domain.repository.DriveBackupInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for BackupRepositoryImpl
 * Tests backup creation, upload, download, and restore operations
 */
class BackupRepositoryImplTest {

    private lateinit var repository: BackupRepositoryImpl
    private val walletDao: WalletDao = mockk()
    private val transactionDao: TransactionDao = mockk()
    private val categoryDao: CategoryDao = mockk()
    private val driveService: DriveService = mockk()
    private val authRepository: AuthRepository = mockk()
    private val authService: AuthService = mockk()
    private val googleSignInAccount: GoogleSignInAccount = mockk()

    private val testUser = User(
        id = "user1",
        email = "test@example.com",
        displayName = "Test User",
        photoUrl = null,
        isGuest = false
    )

    private val testGuestUser = User(
        id = "guest1",
        email = "guest@personalwallet.com",
        displayName = "Guest User",
        photoUrl = null,
        isGuest = true
    )

    private val testWalletEntity = WalletEntity(
        id = "wallet1",
        name = "Test Wallet",
        balance = "1000.00",
        currency = "USD",
        createdAt = 1234567890L,
        updatedAt = 1234567890L,
        isDeleted = false
    )

    private val testWallet = Wallet(
        id = "wallet1",
        name = "Test Wallet",
        balance = BigDecimal("1000.00"),
        currency = "USD",
        createdAt = 1234567890L,
        updatedAt = 1234567890L
    )

    private val testTransactionEntity = TransactionEntity(
        id = "tx1",
        walletId = "wallet1",
        amount = "50.00",
        type = "EXPENSE",
        category = "Food",
        description = "Lunch",
        date = 1234567890L,
        createdAt = 1234567890L,
        updatedAt = 1234567890L,
        isDeleted = false
    )

    private val testTransaction = Transaction(
        id = "tx1",
        walletId = "wallet1",
        amount = BigDecimal("50.00"),
        type = TransactionType.EXPENSE,
        category = "Food",
        description = "Lunch",
        date = 1234567890L,
        createdAt = 1234567890L,
        updatedAt = 1234567890L
    )

    private val testCategoryEntity = CategoryEntity(
        id = "cat1",
        name = "Food",
        color = "#FF0000",
        icon = "food",
        createdAt = 1234567890L,
        isDeleted = false
    )

    private val testCategory = Category(
        id = "cat1",
        name = "Food",
        color = "#FF0000",
        icon = "food",
        createdAt = 1234567890L
    )

    private val testBackupData = BackupData(
        wallets = listOf(testWallet),
        transactions = listOf(testTransaction),
        categories = listOf(testCategory)
    )

    @Before
    fun setup() {
        repository = BackupRepositoryImpl(
            walletDao,
            transactionDao,
            categoryDao,
            driveService,
            authRepository,
            authService
        )
    }

    // ========== createBackup Tests ==========

    @Test
    fun `createBackup should create backup data successfully`() = runTest {
        // Given
        coEvery { walletDao.getAllWalletsSync() } returns listOf(testWalletEntity)
        coEvery { transactionDao.getAllTransactions() } returns flowOf(listOf(testTransactionEntity))
        coEvery { categoryDao.getAllCategories() } returns flowOf(listOf(testCategoryEntity))

        // When
        val result = repository.createBackup()

        // Then
        assertThat(result.isSuccess).isTrue()
        val backupData = result.getOrNull()
        assertThat(backupData).isNotNull()
        assertThat(backupData?.wallets).hasSize(1)
        assertThat(backupData?.transactions).hasSize(1)
        assertThat(backupData?.categories).hasSize(1)
        assertThat(backupData?.wallets?.first()?.id).isEqualTo("wallet1")
    }

    @Test
    fun `createBackup should handle empty database`() = runTest {
        // Given
        coEvery { walletDao.getAllWalletsSync() } returns emptyList()
        coEvery { transactionDao.getAllTransactions() } returns flowOf(emptyList())
        coEvery { categoryDao.getAllCategories() } returns flowOf(emptyList())

        // When
        val result = repository.createBackup()

        // Then
        assertThat(result.isSuccess).isTrue()
        val backupData = result.getOrNull()
        assertThat(backupData?.wallets).isEmpty()
        assertThat(backupData?.transactions).isEmpty()
        assertThat(backupData?.categories).isEmpty()
    }

    @Test
    fun `createBackup should handle dao exception`() = runTest {
        // Given
        coEvery { walletDao.getAllWalletsSync() } throws Exception("Database error")

        // When
        val result = repository.createBackup()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Database error")
    }

    // ========== uploadBackupToDrive Tests ==========

    @Test
    fun `uploadBackupToDrive should upload successfully when user is signed in`() = runTest {
        // Given
        every { googleSignInAccount.email } returns "test@example.com"
        every { authService.getCurrentGoogleAccount() } returns googleSignInAccount
        coEvery { driveService.uploadBackup("test@example.com", testBackupData) } returns Result.success("file123")

        // When
        val result = repository.uploadBackupToDrive(testBackupData)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo("file123")
        coVerify { driveService.uploadBackup("test@example.com", testBackupData) }
    }

    @Test
    fun `uploadBackupToDrive should fail when user is not signed in`() = runTest {
        // Given
        every { authService.getCurrentGoogleAccount() } returns null

        // When
        val result = repository.uploadBackupToDrive(testBackupData)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("User not signed in")
        coVerify(exactly = 0) { driveService.uploadBackup(any(), any()) }
    }

    @Test
    fun `uploadBackupToDrive should fail when user is guest`() = runTest {
        // Given - Now guest users won't have a Google account
        every { authService.getCurrentGoogleAccount() } returns null

        // When
        val result = repository.uploadBackupToDrive(testBackupData)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("User not signed in")
        coVerify(exactly = 0) { driveService.uploadBackup(any(), any()) }
    }

    @Test
    fun `uploadBackupToDrive should fail when email is blank`() = runTest {
        // Given
        every { googleSignInAccount.email } returns ""
        every { authService.getCurrentGoogleAccount() } returns googleSignInAccount

        // When
        val result = repository.uploadBackupToDrive(testBackupData)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("User not signed in")
    }

    @Test
    fun `uploadBackupToDrive should update last backup timestamp on success`() = runTest {
        // Given
        every { googleSignInAccount.email } returns "test@example.com"
        every { authService.getCurrentGoogleAccount() } returns googleSignInAccount
        coEvery { driveService.uploadBackup("test@example.com", testBackupData) } returns Result.success("file123")

        // When
        repository.uploadBackupToDrive(testBackupData)
        val timestamp = repository.getLastBackupTimestamp()

        // Then
        assertThat(timestamp).isNotNull()
    }

    @Test
    fun `uploadBackupToDrive should handle drive service failure`() = runTest {
        // Given
        every { googleSignInAccount.email } returns "test@example.com"
        every { authService.getCurrentGoogleAccount() } returns googleSignInAccount
        coEvery { driveService.uploadBackup("test@example.com", testBackupData) } returns
            Result.failure(Exception("Network error"))

        // When
        val result = repository.uploadBackupToDrive(testBackupData)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Network error")
    }

    // ========== downloadBackupFromDrive Tests ==========

    @Test
    fun `downloadBackupFromDrive should download successfully when user is signed in`() = runTest {
        // Given
        every { googleSignInAccount.email } returns "test@example.com"
        every { authService.getCurrentGoogleAccount() } returns googleSignInAccount
        coEvery { driveService.downloadBackup("test@example.com", "file123") } returns Result.success(testBackupData)

        // When
        val result = repository.downloadBackupFromDrive("file123")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(testBackupData)
        coVerify { driveService.downloadBackup("test@example.com", "file123") }
    }

    @Test
    fun `downloadBackupFromDrive should fail when user is not signed in`() = runTest {
        // Given
        every { authService.getCurrentGoogleAccount() } returns null

        // When
        val result = repository.downloadBackupFromDrive("file123")

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("User not signed in")
        coVerify(exactly = 0) { driveService.downloadBackup(any(), any()) }
    }

    @Test
    fun `downloadBackupFromDrive should fail when user is guest`() = runTest {
        // Given
        every { authService.getCurrentGoogleAccount() } returns null

        // When
        val result = repository.downloadBackupFromDrive("file123")

        // Then
        assertThat(result.isFailure).isTrue()
        coVerify(exactly = 0) { driveService.downloadBackup(any(), any()) }
    }

    // ========== restoreBackup Tests ==========

    @Test
    fun `restoreBackup should restore backup data successfully`() = runTest {
        // Given
        coEvery { walletDao.deleteAllWallets() } returns Unit
        coEvery { transactionDao.deleteAllTransactions() } returns Unit
        coEvery { categoryDao.deleteAllCategories() } returns Unit
        coEvery { walletDao.insertWallet(any()) } returns Unit
        coEvery { transactionDao.insertTransaction(any()) } returns Unit
        coEvery { categoryDao.insertCategory(any()) } returns Unit

        // When
        val result = repository.restoreBackup(testBackupData)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { walletDao.deleteAllWallets() }
        coVerify { transactionDao.deleteAllTransactions() }
        coVerify { categoryDao.deleteAllCategories() }
        coVerify { walletDao.insertWallet(any()) }
        coVerify { transactionDao.insertTransaction(any()) }
        coVerify { categoryDao.insertCategory(any()) }
    }

    @Test
    fun `restoreBackup should handle empty backup data`() = runTest {
        // Given
        val emptyBackup = BackupData(
            wallets = emptyList(),
            transactions = emptyList(),
            categories = emptyList()
        )
        coEvery { walletDao.deleteAllWallets() } returns Unit
        coEvery { transactionDao.deleteAllTransactions() } returns Unit
        coEvery { categoryDao.deleteAllCategories() } returns Unit

        // When
        val result = repository.restoreBackup(emptyBackup)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { walletDao.deleteAllWallets() }
        coVerify { transactionDao.deleteAllTransactions() }
        coVerify { categoryDao.deleteAllCategories() }
        coVerify(exactly = 0) { walletDao.insertWallet(any()) }
        coVerify(exactly = 0) { transactionDao.insertTransaction(any()) }
        coVerify(exactly = 0) { categoryDao.insertCategory(any()) }
    }

    @Test
    fun `restoreBackup should handle dao exception during delete`() = runTest {
        // Given
        coEvery { walletDao.deleteAllWallets() } throws Exception("Delete failed")

        // When
        val result = repository.restoreBackup(testBackupData)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Delete failed")
    }

    @Test
    fun `restoreBackup should handle dao exception during insert`() = runTest {
        // Given
        coEvery { walletDao.deleteAllWallets() } returns Unit
        coEvery { transactionDao.deleteAllTransactions() } returns Unit
        coEvery { categoryDao.deleteAllCategories() } returns Unit
        coEvery { walletDao.insertWallet(any()) } throws Exception("Insert failed")

        // When
        val result = repository.restoreBackup(testBackupData)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Insert failed")
    }

    // ========== listDriveBackups Tests ==========

    @Test
    fun `listDriveBackups should list backups successfully when user is signed in`() = runTest {
        // Given
        val backupInfo = DriveBackupInfo(
            id = "file123",
            name = "wallet_backup_123.json",
            timestamp = 1234567890L,
            size = 1024L
        )
        every { googleSignInAccount.email } returns "test@example.com"
        every { authService.getCurrentGoogleAccount() } returns googleSignInAccount
        coEvery { driveService.listBackups("test@example.com") } returns Result.success(listOf(backupInfo))

        // When
        val result = repository.listDriveBackups()

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).hasSize(1)
        assertThat(result.getOrNull()?.first()?.id).isEqualTo("file123")
    }

    @Test
    fun `listDriveBackups should fail when user is not signed in`() = runTest {
        // Given
        every { authService.getCurrentGoogleAccount() } returns null

        // When
        val result = repository.listDriveBackups()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("User not signed in")
        coVerify(exactly = 0) { driveService.listBackups(any()) }
    }

    @Test
    fun `listDriveBackups should fail when user is guest`() = runTest {
        // Given
        every { authService.getCurrentGoogleAccount() } returns null

        // When
        val result = repository.listDriveBackups()

        // Then
        assertThat(result.isFailure).isTrue()
        coVerify(exactly = 0) { driveService.listBackups(any()) }
    }

    // ========== getLastBackupTimestamp Tests ==========

    @Test
    fun `getLastBackupTimestamp should return null initially`() = runTest {
        // When
        val timestamp = repository.getLastBackupTimestamp()

        // Then
        assertThat(timestamp).isNull()
    }

    @Test
    fun `getLastBackupTimestamp should return timestamp after successful upload`() = runTest {
        // Given
        every { authRepository.getCurrentUser() } returns flowOf(testUser)
        coEvery { driveService.uploadBackup("test@example.com", testBackupData) } returns Result.success("file123")

        // When
        repository.uploadBackupToDrive(testBackupData)
        val timestamp = repository.getLastBackupTimestamp()

        // Then
        assertThat(timestamp).isNotNull()
        assertThat(timestamp).isGreaterThan(0L)
    }

    // ========== Edge Cases and Integration Tests ==========

    @Test
    fun `full backup and restore cycle should work correctly`() = runTest {
        // Given - Create
        coEvery { walletDao.getAllWalletsSync() } returns listOf(testWalletEntity)
        coEvery { transactionDao.getAllTransactions() } returns flowOf(listOf(testTransactionEntity))
        coEvery { categoryDao.getAllCategories() } returns flowOf(listOf(testCategoryEntity))

        // Given - Upload
        every { googleSignInAccount.email } returns "test@example.com"
        every { authService.getCurrentGoogleAccount() } returns googleSignInAccount
        coEvery { driveService.uploadBackup(any(), any()) } returns Result.success("file123")

        // Given - Restore
        coEvery { walletDao.deleteAllWallets() } returns Unit
        coEvery { transactionDao.deleteAllTransactions() } returns Unit
        coEvery { categoryDao.deleteAllCategories() } returns Unit
        coEvery { walletDao.insertWallet(any()) } returns Unit
        coEvery { transactionDao.insertTransaction(any()) } returns Unit
        coEvery { categoryDao.insertCategory(any()) } returns Unit

        // When
        val createResult = repository.createBackup()
        val backupData = createResult.getOrNull()!!
        val uploadResult = repository.uploadBackupToDrive(backupData)
        val restoreResult = repository.restoreBackup(backupData)

        // Then
        assertThat(createResult.isSuccess).isTrue()
        assertThat(uploadResult.isSuccess).isTrue()
        assertThat(restoreResult.isSuccess).isTrue()
    }

    @Test
    fun `setCurrentUser should be no-op`() = runTest {
        // This method is deprecated but still in interface for backward compatibility
        // When
        repository.setCurrentUser("test@example.com")

        // Then - Should not throw or cause issues
        // No assertions needed, just verify it doesn't crash
    }
}

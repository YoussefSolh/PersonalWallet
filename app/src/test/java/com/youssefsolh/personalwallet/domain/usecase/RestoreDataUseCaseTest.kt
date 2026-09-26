package com.youssefsolh.personalwallet.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.domain.model.BackupData
import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.BackupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for RestoreDataUseCase
 * Tests the backup download and restore flow
 */
class RestoreDataUseCaseTest {

    private lateinit var useCase: RestoreDataUseCase
    private val backupRepository: BackupRepository = mockk()

    private val testWallet = Wallet(
        id = "wallet1",
        name = "Test Wallet",
        balance = BigDecimal("1000.00"),
        currency = "USD",
        createdAt = 1234567890L,
        updatedAt = 1234567890L
    )

    private val testTransaction = Transaction(
        id = "tx1",
        fromWalletId = "wallet1",
        toWalletId = null,
        amount = BigDecimal("50.00"),
        type = TransactionType.EXPENSE,
        categoryId = "Food",
        description = "Lunch",
        timestamp = 1234567890L,
        createdAt = 1234567890L,
        updatedAt = 1234567890L
    )

    private val testCategory = Category(
        id = "cat1",
        name = "Food",
        color = "#FF0000",
        icon = "food",
        type = TransactionType.EXPENSE,
        createdAt = 1234567890L
    )

    private val testBackupData = BackupData(
        wallets = listOf(testWallet),
        transactions = listOf(testTransaction),
        categories = listOf(testCategory)
    )

    @Before
    fun setup() {
        useCase = RestoreDataUseCase(backupRepository)
    }

    @Test
    fun `invoke should download and restore backup successfully`() = runTest {
        // Given
        val fileId = "file123"
        coEvery { backupRepository.downloadBackupFromDrive(fileId) } returns Result.success(testBackupData)
        coEvery { backupRepository.restoreBackup(testBackupData) } returns Result.success(Unit)

        // When
        val result = useCase(fileId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { backupRepository.downloadBackupFromDrive(fileId) }
        coVerify { backupRepository.restoreBackup(testBackupData) }
    }

    @Test
    fun `invoke should fail when download fails`() = runTest {
        // Given
        val fileId = "file123"
        val exception = Exception("User not signed in")
        coEvery { backupRepository.downloadBackupFromDrive(fileId) } returns Result.failure(exception)

        // When
        val result = useCase(fileId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("User not signed in")
        coVerify { backupRepository.downloadBackupFromDrive(fileId) }
        coVerify(exactly = 0) { backupRepository.restoreBackup(any()) }
    }

    @Test
    fun `invoke should fail when restore fails`() = runTest {
        // Given
        val fileId = "file123"
        val restoreException = Exception("Database error during restore")
        coEvery { backupRepository.downloadBackupFromDrive(fileId) } returns Result.success(testBackupData)
        coEvery { backupRepository.restoreBackup(testBackupData) } returns Result.failure(restoreException)

        // When
        val result = useCase(fileId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Database error during restore")
        coVerify { backupRepository.downloadBackupFromDrive(fileId) }
        coVerify { backupRepository.restoreBackup(testBackupData) }
    }

    @Test
    fun `invoke should handle empty backup data`() = runTest {
        // Given
        val fileId = "file123"
        val emptyBackup = BackupData(
            wallets = emptyList(),
            transactions = emptyList(),
            categories = emptyList()
        )
        coEvery { backupRepository.downloadBackupFromDrive(fileId) } returns Result.success(emptyBackup)
        coEvery { backupRepository.restoreBackup(emptyBackup) } returns Result.success(Unit)

        // When
        val result = useCase(fileId)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `invoke should handle network errors during download`() = runTest {
        // Given
        val fileId = "file123"
        val networkException = Exception("Network timeout")
        coEvery { backupRepository.downloadBackupFromDrive(fileId) } returns Result.failure(networkException)

        // When
        val result = useCase(fileId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Network timeout")
    }

    @Test
    fun `invoke should handle invalid file ID`() = runTest {
        // Given
        val invalidFileId = ""
        val exception = Exception("Invalid file ID")
        coEvery { backupRepository.downloadBackupFromDrive(invalidFileId) } returns Result.failure(exception)

        // When
        val result = useCase(invalidFileId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Invalid file ID")
    }

    @Test
    fun `invoke should handle corrupted backup data`() = runTest {
        // Given
        val fileId = "file123"
        val corruptedException = Exception("Failed to parse backup data")
        coEvery { backupRepository.downloadBackupFromDrive(fileId) } returns Result.failure(corruptedException)

        // When
        val result = useCase(fileId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Failed to parse backup data")
    }

    @Test
    fun `invoke should handle large backup data`() = runTest {
        // Given
        val fileId = "file123"
        val largeBackupData = BackupData(
            wallets = List(100) { index ->
                Wallet(
                    id = "wallet$index",
                    name = "Wallet $index",
                    balance = BigDecimal("1000.00"),
                    currency = "USD",
                    createdAt = 1234567890L,
                    updatedAt = 1234567890L
                )
            },
            transactions = List(1000) { index ->
                Transaction(
                    id = "tx$index",
                    fromWalletId = "wallet${index % 100}",
                    toWalletId = null,
                    amount = BigDecimal("50.00"),
                    type = if (index % 2 == 0) TransactionType.EXPENSE else TransactionType.INCOME,
                    categoryId = "Category",
                    description = "Transaction $index",
                    timestamp = 1234567890L,
                    createdAt = 1234567890L,
                    updatedAt = 1234567890L
                )
            },
            categories = List(50) { index ->
                Category(
                    id = "cat$index",
                    name = "Category $index",
                    color = "#FF0000",
                    icon = "icon$index",
                    type = TransactionType.EXPENSE,
                    createdAt = 1234567890L
                )
            }
        )
        coEvery { backupRepository.downloadBackupFromDrive(fileId) } returns Result.success(largeBackupData)
        coEvery { backupRepository.restoreBackup(largeBackupData) } returns Result.success(Unit)

        // When
        val result = useCase(fileId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { backupRepository.downloadBackupFromDrive(fileId) }
        coVerify { backupRepository.restoreBackup(largeBackupData) }
    }

    @Test
    fun `invoke should maintain call order - download before restore`() = runTest {
        // Given
        val fileId = "file123"
        val callOrder = mutableListOf<String>()
        coEvery { backupRepository.downloadBackupFromDrive(fileId) } coAnswers {
            callOrder.add("download")
            Result.success(testBackupData)
        }
        coEvery { backupRepository.restoreBackup(testBackupData) } coAnswers {
            callOrder.add("restore")
            Result.success(Unit)
        }

        // When
        useCase(fileId)

        // Then
        assertThat(callOrder).isEqualTo(listOf("download", "restore"))
    }

    @Test
    fun `invoke should handle guest user attempting restore`() = runTest {
        // Given
        val fileId = "file123"
        val guestException = Exception("User not signed in")
        coEvery { backupRepository.downloadBackupFromDrive(fileId) } returns Result.failure(guestException)

        // When
        val result = useCase(fileId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("User not signed in")
    }

    @Test
    fun `invoke should handle file not found error`() = runTest {
        // Given
        val fileId = "nonexistent_file"
        val notFoundException = Exception("File not found")
        coEvery { backupRepository.downloadBackupFromDrive(fileId) } returns Result.failure(notFoundException)

        // When
        val result = useCase(fileId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("File not found")
    }

    @Test
    fun `invoke should verify repository calls with correct file ID`() = runTest {
        // Given
        val specificFileId = "backup_2024_01_01_abc123"
        coEvery { backupRepository.downloadBackupFromDrive(specificFileId) } returns Result.success(testBackupData)
        coEvery { backupRepository.restoreBackup(testBackupData) } returns Result.success(Unit)

        // When
        useCase(specificFileId)

        // Then
        coVerify(exactly = 1) { backupRepository.downloadBackupFromDrive(specificFileId) }
        coVerify(exactly = 1) { backupRepository.restoreBackup(testBackupData) }
    }
}

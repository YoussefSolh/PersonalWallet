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
 * Unit tests for BackupDataUseCase
 * Tests the backup creation and upload flow
 */
class BackupDataUseCaseTest {

    private lateinit var useCase: BackupDataUseCase
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
        useCase = BackupDataUseCase(backupRepository)
    }

    @Test
    fun `invoke should create and upload backup successfully`() = runTest {
        // Given
        coEvery { backupRepository.createBackup() } returns Result.success(testBackupData)
        coEvery { backupRepository.uploadBackupToDrive(testBackupData) } returns Result.success("file123")

        // When
        val result = useCase()

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo("file123")
        coVerify { backupRepository.createBackup() }
        coVerify { backupRepository.uploadBackupToDrive(testBackupData) }
    }

    @Test
    fun `invoke should fail when backup creation fails`() = runTest {
        // Given
        val exception = Exception("Database error")
        coEvery { backupRepository.createBackup() } returns Result.failure(exception)

        // When
        val result = useCase()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Database error")
        coVerify { backupRepository.createBackup() }
        coVerify(exactly = 0) { backupRepository.uploadBackupToDrive(any()) }
    }

    @Test
    fun `invoke should fail when upload fails`() = runTest {
        // Given
        val uploadException = Exception("User not signed in")
        coEvery { backupRepository.createBackup() } returns Result.success(testBackupData)
        coEvery { backupRepository.uploadBackupToDrive(testBackupData) } returns Result.failure(uploadException)

        // When
        val result = useCase()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("User not signed in")
        coVerify { backupRepository.createBackup() }
        coVerify { backupRepository.uploadBackupToDrive(testBackupData) }
    }

    @Test
    fun `invoke should handle empty backup data`() = runTest {
        // Given
        val emptyBackup = BackupData(
            wallets = emptyList(),
            transactions = emptyList(),
            categories = emptyList()
        )
        coEvery { backupRepository.createBackup() } returns Result.success(emptyBackup)
        coEvery { backupRepository.uploadBackupToDrive(emptyBackup) } returns Result.success("file123")

        // When
        val result = useCase()

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo("file123")
    }

    @Test
    fun `invoke should handle network errors during upload`() = runTest {
        // Given
        val networkException = Exception("Failed to upload backup: Network error")
        coEvery { backupRepository.createBackup() } returns Result.success(testBackupData)
        coEvery { backupRepository.uploadBackupToDrive(testBackupData) } returns Result.failure(networkException)

        // When
        val result = useCase()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Network error")
    }

    @Test
    fun `invoke should handle guest user scenario`() = runTest {
        // Given
        val guestException = Exception("User not signed in. Please sign in with a Google account to use backup.")
        coEvery { backupRepository.createBackup() } returns Result.success(testBackupData)
        coEvery { backupRepository.uploadBackupToDrive(testBackupData) } returns Result.failure(guestException)

        // When
        val result = useCase()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Please sign in with a Google account")
    }

    @Test
    fun `invoke should return file ID on successful backup`() = runTest {
        // Given
        val expectedFileId = "backup_file_abc123"
        coEvery { backupRepository.createBackup() } returns Result.success(testBackupData)
        coEvery { backupRepository.uploadBackupToDrive(testBackupData) } returns Result.success(expectedFileId)

        // When
        val result = useCase()

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedFileId)
    }

    @Test
    fun `invoke should handle large backup data`() = runTest {
        // Given
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
        coEvery { backupRepository.createBackup() } returns Result.success(largeBackupData)
        coEvery { backupRepository.uploadBackupToDrive(largeBackupData) } returns Result.success("file123")

        // When
        val result = useCase()

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { backupRepository.createBackup() }
        coVerify { backupRepository.uploadBackupToDrive(largeBackupData) }
    }

    @Test
    fun `invoke should maintain call order - create before upload`() = runTest {
        // Given
        val callOrder = mutableListOf<String>()
        coEvery { backupRepository.createBackup() } coAnswers {
            callOrder.add("create")
            Result.success(testBackupData)
        }
        coEvery { backupRepository.uploadBackupToDrive(testBackupData) } coAnswers {
            callOrder.add("upload")
            Result.success("file123")
        }

        // When
        useCase()

        // Then
        assertThat(callOrder).isEqualTo(listOf("create", "upload"))
    }

    @Test
    fun `invoke should handle null values in backup data gracefully`() = runTest {
        // Given
        val backupWithOptionalFields = BackupData(
            wallets = listOf(
                testWallet.copy(balance = BigDecimal.ZERO)
            ),
            transactions = listOf(
                testTransaction.copy(description = "")
            ),
            categories = listOf(testCategory)
        )
        coEvery { backupRepository.createBackup() } returns Result.success(backupWithOptionalFields)
        coEvery { backupRepository.uploadBackupToDrive(backupWithOptionalFields) } returns Result.success("file123")

        // When
        val result = useCase()

        // Then
        assertThat(result.isSuccess).isTrue()
    }
}

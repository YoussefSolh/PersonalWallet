package com.youssefsolh.personalwallet.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.data.local.CurrentUserProvider
import com.youssefsolh.personalwallet.data.local.dao.CategoryDao
import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.data.local.dao.WalletDao
import com.youssefsolh.personalwallet.data.local.entity.CategoryEntity
import com.youssefsolh.personalwallet.data.local.entity.TransactionEntity
import com.youssefsolh.personalwallet.data.local.entity.WalletEntity
import com.youssefsolh.personalwallet.data.local.entity.WalletWithCurrencySymbol
import com.youssefsolh.personalwallet.data.remote.auth.AuthService
import com.youssefsolh.personalwallet.data.remote.drive.DriveService
import com.youssefsolh.personalwallet.domain.model.BackupData
import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.AuthRepository
import com.youssefsolh.personalwallet.domain.repository.DriveBackupInfo
import com.youssefsolh.personalwallet.domain.repository.TransactionRunner
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for BackupRepositoryImpl: backup creation, Drive access gating on a signed-in
 * Google account, and user-scoped, transactional restore.
 */
class BackupRepositoryImplTest {

    private lateinit var repository: BackupRepositoryImpl
    private val walletDao: WalletDao = mockk(relaxed = true)
    private val transactionDao: TransactionDao = mockk(relaxed = true)
    private val categoryDao: CategoryDao = mockk(relaxed = true)
    private val driveService: DriveService = mockk()
    private val authRepository: AuthRepository = mockk()
    private val authService: AuthService = mockk()
    private val currentUserProvider: CurrentUserProvider = mockk()
    private val googleSignInAccount: GoogleSignInAccount = mockk()

    /** Records whether DAO writes happened inside the transaction runner. */
    private var inTransaction = false
    private var transactionCount = 0
    private val transactionRunner = object : TransactionRunner {
        override suspend fun <R> runInTransaction(block: suspend () -> R): R {
            transactionCount++
            inTransaction = true
            try {
                return block()
            } finally {
                inTransaction = false
            }
        }
    }

    private val walletRow = WalletWithCurrencySymbol(
        id = "wallet1",
        userId = USER_ID,
        name = "Test Wallet",
        balance = "1000.00",
        currency = "USD",
        currencySymbol = "$",
        createdAt = 1L,
        updatedAt = 1L,
        isDeleted = false
    )

    private val transactionEntity = TransactionEntity(
        id = "tx1",
        userId = USER_ID,
        amount = "50.00",
        type = "EXPENSE",
        description = "Lunch",
        categoryId = "custom1",
        fromWalletId = "wallet1",
        toWalletId = null
    )

    private val customCategoryEntity = CategoryEntity(
        id = "custom1",
        userId = USER_ID,
        name = "Coffee",
        icon = "coffee",
        color = "#6F4E37",
        type = "EXPENSE",
        isCustom = true
    )

    private val wallet = Wallet(id = "wallet1", name = "Test Wallet", balance = BigDecimal("1000.00"))
    private val transaction = Transaction(
        id = "tx1",
        amount = BigDecimal("50.00"),
        type = TransactionType.EXPENSE,
        description = "Lunch",
        categoryId = "custom1",
        fromWalletId = "wallet1",
        toWalletId = null
    )
    private val customCategory = Category(
        id = "custom1", name = "Coffee", icon = "coffee", color = "#6F4E37",
        type = TransactionType.EXPENSE, isCustom = true
    )
    private val defaultCategory = Category(
        id = "food", name = "Food", icon = "food", color = "#FF0000",
        type = TransactionType.EXPENSE, isDefault = true
    )
    private val backupData = BackupData(
        wallets = listOf(wallet),
        transactions = listOf(transaction),
        categories = listOf(defaultCategory, customCategory)
    )

    @Before
    fun setup() {
        coEvery { currentUserProvider.getCurrentUserId() } returns USER_ID
        repository = BackupRepositoryImpl(
            walletDao,
            transactionDao,
            categoryDao,
            driveService,
            authRepository,
            authService,
            currentUserProvider,
            transactionRunner
        )
    }

    // ========== createBackup ==========

    @Test
    fun `createBackup collects the current user's data`() = runTest {
        coEvery { walletDao.getAllWalletsSync(USER_ID) } returns listOf(walletRow)
        every { transactionDao.getAllTransactions(USER_ID) } returns flowOf(listOf(transactionEntity))
        every { categoryDao.getAllCategories(USER_ID) } returns flowOf(listOf(customCategoryEntity))

        val result = repository.createBackup()

        assertThat(result.isSuccess).isTrue()
        val data = result.getOrThrow()
        assertThat(data.wallets.map { it.id }).containsExactly("wallet1")
        assertThat(data.transactions.map { it.id }).containsExactly("tx1")
        assertThat(data.categories.map { it.id }).containsExactly("custom1")
    }

    @Test
    fun `createBackup returns failure when the dao throws`() = runTest {
        coEvery { walletDao.getAllWalletsSync(USER_ID) } throws IllegalStateException("Database error")

        val result = repository.createBackup()

        assertThat(result.isFailure).isTrue()
    }

    // ========== Drive access ==========

    @Test
    fun `upload succeeds for a signed-in Google account and records the timestamp`() = runTest {
        every { googleSignInAccount.email } returns EMAIL
        every { authService.getCurrentGoogleAccount() } returns googleSignInAccount
        coEvery { driveService.uploadBackup(EMAIL, backupData) } returns Result.success("file123")

        val result = repository.uploadBackupToDrive(backupData)

        assertThat(result.getOrNull()).isEqualTo("file123")
        assertThat(repository.getLastBackupTimestamp()).isNotNull()
    }

    @Test
    fun `upload fails without a Google account`() = runTest {
        every { authService.getCurrentGoogleAccount() } returns null

        val result = repository.uploadBackupToDrive(backupData)

        assertThat(result.isFailure).isTrue()
        assertThat(repository.getLastBackupTimestamp()).isNull()
        coVerify(exactly = 0) { driveService.uploadBackup(any(), any()) }
    }

    @Test
    fun `upload fails when the account email is blank`() = runTest {
        every { googleSignInAccount.email } returns "  "
        every { authService.getCurrentGoogleAccount() } returns googleSignInAccount

        assertThat(repository.uploadBackupToDrive(backupData).isFailure).isTrue()
        coVerify(exactly = 0) { driveService.uploadBackup(any(), any()) }
    }

    @Test
    fun `download and list fail without a Google account`() = runTest {
        every { authService.getCurrentGoogleAccount() } returns null

        assertThat(repository.downloadBackupFromDrive("file123").isFailure).isTrue()
        assertThat(repository.listDriveBackups().isFailure).isTrue()
    }

    @Test
    fun `listDriveBackups returns the Drive listing`() = runTest {
        val info = DriveBackupInfo(id = "file123", name = "backup.json", timestamp = 1L, size = 10L)
        every { googleSignInAccount.email } returns EMAIL
        every { authService.getCurrentGoogleAccount() } returns googleSignInAccount
        coEvery { driveService.listBackups(EMAIL) } returns Result.success(listOf(info))

        assertThat(repository.listDriveBackups().getOrThrow()).containsExactly(info)
    }

    // ========== restoreBackup ==========

    @Test
    fun `restore clears only the current user's data inside one transaction`() = runTest {
        val deletesInTransaction = mutableListOf<Boolean>()
        coEvery { walletDao.deleteAllWalletsForUser(USER_ID) } answers { deletesInTransaction += inTransaction }
        coEvery { transactionDao.deleteAllTransactionsForUser(USER_ID) } answers { deletesInTransaction += inTransaction }
        coEvery { categoryDao.deleteAllCategoriesForUser(USER_ID) } answers { deletesInTransaction += inTransaction }

        val result = repository.restoreBackup(backupData)

        assertThat(result.isSuccess).isTrue()
        assertThat(transactionCount).isEqualTo(1)
        assertThat(deletesInTransaction).containsExactly(true, true, true)
    }

    @Test
    fun `restore inserts rows under the current user and skips shared default categories`() = runTest {
        val walletSlot = slot<WalletEntity>()
        val transactionSlot = slot<TransactionEntity>()
        val categories = mutableListOf<CategoryEntity>()
        coEvery { walletDao.insertWallet(capture(walletSlot)) } returns Unit
        coEvery { transactionDao.insertTransaction(capture(transactionSlot)) } returns Unit
        coEvery { categoryDao.insertCategory(capture(categories)) } returns Unit

        repository.restoreBackup(backupData).getOrThrow()

        assertThat(walletSlot.captured.userId).isEqualTo(USER_ID)
        assertThat(transactionSlot.captured.userId).isEqualTo(USER_ID)
        assertThat(categories.map { it.id }).containsExactly("custom1")
        assertThat(categories.single().userId).isEqualTo(USER_ID)
    }

    @Test
    fun `restore failure is reported and happens inside the transaction`() = runTest {
        coEvery { walletDao.insertWallet(any()) } throws IllegalStateException("Insert failed")

        val result = repository.restoreBackup(backupData)

        assertThat(result.isFailure).isTrue()
        assertThat(transactionCount).isEqualTo(1)
    }

    @Test
    fun `restore of empty backup only clears user data`() = runTest {
        repository.restoreBackup(BackupData(emptyList(), emptyList(), emptyList())).getOrThrow()

        coVerify { walletDao.deleteAllWalletsForUser(USER_ID) }
        coVerify(exactly = 0) { walletDao.insertWallet(any()) }
        coVerify(exactly = 0) { transactionDao.insertTransaction(any()) }
        coVerify(exactly = 0) { categoryDao.insertCategory(any()) }
    }

    companion object {
        private const val USER_ID = "user1"
        private const val EMAIL = "test@example.com"
    }
}

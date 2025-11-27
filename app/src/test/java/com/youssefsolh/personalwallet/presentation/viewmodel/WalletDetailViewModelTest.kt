package com.youssefsolh.personalwallet.presentation.viewmodel

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.data.util.CsvExporter
import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.CategoryRepository
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import com.youssefsolh.personalwallet.domain.usecase.DeleteTransactionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for WalletDetailViewModel
 * Tests wallet details, transaction filtering, search, and export functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WalletDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: WalletDetailViewModel
    private val walletRepository: WalletRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val deleteTransactionUseCase: DeleteTransactionUseCase = mockk()
    private val csvExporter: CsvExporter = mockk()

    private val testWallet = Wallet(
        id = "wallet1",
        name = "Test Wallet",
        balance = BigDecimal("1000"),
        currency = "USD",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private val testCategory = Category(
        id = "cat1",
        name = "Food",
        icon = "🍔",
        color = 0xFF0000,
        type = TransactionType.EXPENSE
    )

    private val testTransaction = Transaction(
        id = "tx1",
        fromWalletId = "wallet1",
        toWalletId = null,
        categoryId = "cat1",
        amount = BigDecimal("50"),
        description = "Lunch",
        timestamp = System.currentTimeMillis(),
        type = TransactionType.EXPENSE,
        isDeleted = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WalletDetailViewModel(
            walletRepository = walletRepository,
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            deleteTransactionUseCase = deleteTransactionUseCase,
            csvExporter = csvExporter
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have correct defaults`() {
        // When - Get initial state
        val state = viewModel.uiState.value

        // Then
        assertThat(state.wallet).isNull()
        assertThat(state.transactionsWithCategories).isEmpty()
        assertThat(state.searchQuery).isEmpty()
        assertThat(state.filterType).isNull()
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `loading wallet should populate state with wallet and transactions`() = runTest {
        // Given
        coEvery { walletRepository.getWalletById("wallet1") } returns testWallet
        coEvery {
            transactionRepository.getTransactionsByWalletFiltered("wallet1", null, null)
        } returns flowOf(listOf(testTransaction))
        coEvery { categoryRepository.getCategoryById("cat1") } returns testCategory

        // When
        viewModel.loadWallet("wallet1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.wallet).isEqualTo(testWallet)
        assertThat(state.transactionsWithCategories).hasSize(1)
        assertThat(state.transactionsWithCategories[0].transaction).isEqualTo(testTransaction)
        assertThat(state.transactionsWithCategories[0].category).isEqualTo(testCategory)
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `loading non-existent wallet should set error state`() = runTest {
        // Given
        coEvery { walletRepository.getWalletById("nonexistent") } returns null

        // When
        viewModel.loadWallet("nonexistent")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.wallet).isNull()
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isEqualTo("Wallet not found")
    }

    @Test
    fun `search query should debounce and filter transactions`() = runTest {
        // Given
        coEvery { walletRepository.getWalletById("wallet1") } returns testWallet
        coEvery {
            transactionRepository.getTransactionsByWalletFiltered("wallet1", null, null)
        } returns flowOf(listOf(testTransaction))
        coEvery {
            transactionRepository.getTransactionsByWalletFiltered("wallet1", null, "Lunch")
        } returns flowOf(listOf(testTransaction))
        coEvery { categoryRepository.getCategoryById("cat1") } returns testCategory

        // Load wallet first
        viewModel.loadWallet("wallet1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When - Set search query
        viewModel.onSearchQueryChanged("Lunch")

        // Advance time to trigger debounce (300ms)
        advanceTimeBy(300)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.searchQuery).isEqualTo("Lunch")
        coVerify {
            transactionRepository.getTransactionsByWalletFiltered("wallet1", null, "Lunch")
        }
    }

    @Test
    fun `filter by transaction type should reload transactions`() = runTest {
        // Given
        coEvery { walletRepository.getWalletById("wallet1") } returns testWallet
        coEvery {
            transactionRepository.getTransactionsByWalletFiltered("wallet1", null, null)
        } returns flowOf(listOf(testTransaction))
        coEvery {
            transactionRepository.getTransactionsByWalletFiltered("wallet1", TransactionType.EXPENSE, null)
        } returns flowOf(listOf(testTransaction))
        coEvery { categoryRepository.getCategoryById("cat1") } returns testCategory

        // Load wallet first
        viewModel.loadWallet("wallet1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When - Set filter type
        viewModel.onFilterTypeChanged(TransactionType.EXPENSE)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.filterType).isEqualTo(TransactionType.EXPENSE)
        coVerify {
            transactionRepository.getTransactionsByWalletFiltered("wallet1", TransactionType.EXPENSE, null)
        }
    }

    @Test
    fun `deleting transaction should reload wallet`() = runTest {
        // Given
        coEvery { walletRepository.getWalletById("wallet1") } returns testWallet
        coEvery {
            transactionRepository.getTransactionsByWalletFiltered(any(), any(), any())
        } returns flowOf(emptyList())
        coEvery { deleteTransactionUseCase(testTransaction) } returns Result.success(Unit)

        // Load wallet first
        viewModel.loadWallet("wallet1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When - Delete transaction
        viewModel.deleteTransaction(testTransaction)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { deleteTransactionUseCase(testTransaction) }
        // Wallet should be reloaded
        coVerify(atLeast = 2) { walletRepository.getWalletById("wallet1") }
    }

    @Test
    fun `delete transaction failure should set error state`() = runTest {
        // Given
        val errorMessage = "Failed to delete"
        coEvery { walletRepository.getWalletById("wallet1") } returns testWallet
        coEvery {
            transactionRepository.getTransactionsByWalletFiltered(any(), any(), any())
        } returns flowOf(emptyList())
        coEvery { deleteTransactionUseCase(testTransaction) } returns Result.failure(Exception(errorMessage))

        // Load wallet first
        viewModel.loadWallet("wallet1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When - Delete transaction with error
        viewModel.deleteTransaction(testTransaction)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.error).isEqualTo(errorMessage)
    }

    @Test
    fun `refresh should reload wallet and transactions`() = runTest {
        // Given
        coEvery { walletRepository.getWalletById("wallet1") } returns testWallet
        coEvery {
            transactionRepository.getTransactionsByWalletFiltered(any(), any(), any())
        } returns flowOf(emptyList())

        // Load wallet first
        viewModel.loadWallet("wallet1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When - Refresh
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Wallet should be loaded at least twice (initial + refresh)
        coVerify(atLeast = 2) { walletRepository.getWalletById("wallet1") }
    }

    @Test
    fun `export to CSV with no transactions should set error`() = runTest {
        // Given - State with wallet but no transactions
        coEvery { walletRepository.getWalletById("wallet1") } returns testWallet
        coEvery {
            transactionRepository.getTransactionsByWalletFiltered(any(), any(), any())
        } returns flowOf(emptyList())

        viewModel.loadWallet("wallet1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val intent = viewModel.exportToCsv(detailed = false)

        // Then
        assertThat(intent).isNull()
        assertThat(viewModel.uiState.value.error).isEqualTo("No transactions to export")
    }

    @Test
    fun `export to CSV success should return share intent`() = runTest {
        // Given
        val mockIntent = mockk<Intent>(relaxed = true)
        val mockUri = mockk<android.net.Uri>()
        val transactionWithCategory = TransactionWithCategory(
            transaction = testTransaction,
            category = testCategory,
            currentWalletId = "wallet1"
        )

        coEvery { walletRepository.getWalletById("wallet1") } returns testWallet
        coEvery {
            transactionRepository.getTransactionsByWalletFiltered(any(), any(), any())
        } returns flowOf(listOf(testTransaction))
        coEvery { categoryRepository.getCategoryById("cat1") } returns testCategory

        every {
            csvExporter.exportTransactionsToCsv(any(), any())
        } returns Result.success(mockUri)
        every { csvExporter.createShareIntent(mockUri) } returns mockIntent

        viewModel.loadWallet("wallet1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val intent = viewModel.exportToCsv(detailed = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(intent).isEqualTo(mockIntent)
        assertThat(viewModel.uiState.value.exportSuccess).isEqualTo("CSV exported successfully")
    }

    @Test
    fun `clearing error should reset error state`() = runTest {
        // Given - Manually set error
        coEvery { walletRepository.getWalletById("wallet1") } throws Exception("Test error")
        viewModel.loadWallet("wallet1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isNotNull()

        // When
        viewModel.clearError()

        // Then
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `clearing export success should reset export success state`() = runTest {
        // Given
        val mockIntent = mockk<Intent>(relaxed = true)
        val mockUri = mockk<android.net.Uri>()

        coEvery { walletRepository.getWalletById("wallet1") } returns testWallet
        coEvery {
            transactionRepository.getTransactionsByWalletFiltered(any(), any(), any())
        } returns flowOf(listOf(testTransaction))
        coEvery { categoryRepository.getCategoryById("cat1") } returns testCategory
        every {
            csvExporter.exportTransactionsToCsv(any(), any())
        } returns Result.success(mockUri)
        every { csvExporter.createShareIntent(mockUri) } returns mockIntent

        viewModel.loadWallet("wallet1")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.exportToCsv(detailed = false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.exportSuccess).isNotNull()

        // When
        viewModel.clearExportSuccess()

        // Then
        assertThat(viewModel.uiState.value.exportSuccess).isNull()
    }

    @Test
    fun `TransactionWithCategory displayAmount should format correctly for expense`() {
        // Given
        val transactionWithCategory = TransactionWithCategory(
            transaction = testTransaction,
            category = testCategory,
            currentWalletId = "wallet1"
        )

        // Then
        assertThat(transactionWithCategory.displayAmount).isEqualTo("-$50")
        assertThat(transactionWithCategory.displayColor).isFalse()
    }

    @Test
    fun `TransactionWithCategory displayAmount should format correctly for income`() {
        // Given
        val incomeTransaction = testTransaction.copy(type = TransactionType.INCOME)
        val transactionWithCategory = TransactionWithCategory(
            transaction = incomeTransaction,
            category = testCategory,
            currentWalletId = "wallet1"
        )

        // Then
        assertThat(transactionWithCategory.displayAmount).isEqualTo("+$50")
        assertThat(transactionWithCategory.displayColor).isTrue()
    }

    @Test
    fun `TransactionWithCategory displayAmount should format correctly for transfer out`() {
        // Given
        val transferTransaction = testTransaction.copy(
            type = TransactionType.TRANSFER,
            fromWalletId = "wallet1",
            toWalletId = "wallet2"
        )
        val transactionWithCategory = TransactionWithCategory(
            transaction = transferTransaction,
            category = null,
            currentWalletId = "wallet1" // Current wallet is the source
        )

        // Then
        assertThat(transactionWithCategory.displayAmount).isEqualTo("-$50")
        assertThat(transactionWithCategory.displayColor).isFalse()
    }

    @Test
    fun `TransactionWithCategory displayAmount should format correctly for transfer in`() {
        // Given
        val transferTransaction = testTransaction.copy(
            type = TransactionType.TRANSFER,
            fromWalletId = "wallet2",
            toWalletId = "wallet1"
        )
        val transactionWithCategory = TransactionWithCategory(
            transaction = transferTransaction,
            category = null,
            currentWalletId = "wallet1" // Current wallet is the destination
        )

        // Then
        assertThat(transactionWithCategory.displayAmount).isEqualTo("+$50")
        assertThat(transactionWithCategory.displayColor).isTrue()
    }
}

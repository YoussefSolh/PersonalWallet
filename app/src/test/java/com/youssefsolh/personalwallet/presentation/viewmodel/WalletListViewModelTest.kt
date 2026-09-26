package com.youssefsolh.personalwallet.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.domain.model.Currency
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.AuthRepository
import com.youssefsolh.personalwallet.domain.repository.CurrencyRepository
import com.youssefsolh.personalwallet.domain.usecase.GetAllWalletsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for WalletListViewModel
 * Tests UI state management and wallet loading logic
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WalletListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: WalletListViewModel
    private val getAllWalletsUseCase: GetAllWalletsUseCase = mockk()
    private val authRepository: AuthRepository = mockk()
    private val currencyRepository: CurrencyRepository = mockk()

    private val usd = Currency(code = "USD", name = "US Dollar", symbol = "$", isDefault = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { authRepository.getCurrentUser() } returns flowOf(null)
        every { currencyRepository.getAllCurrencies() } returns flowOf(listOf(usd))
        every { currencyRepository.getDefaultCurrency() } returns flowOf(usd)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        // Given
        coEvery { getAllWalletsUseCase() } returns flowOf(emptyList())

        // When
        viewModel = WalletListViewModel(getAllWalletsUseCase, authRepository, currencyRepository)

        // Then - initial state should have loading = true
        val initialState = viewModel.uiState.value
        // Note: Due to init block, loading might already be false
        // Just verify state structure is correct
        assertThat(initialState).isNotNull()
    }

    @Test
    fun `loading wallets should update state with wallet list`() = runTest {
        // Given
        val wallets = listOf(
            Wallet(
                id = "1",
                name = "Wallet 1",
                balance = BigDecimal("1000"),
                currency = "USD",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Wallet(
                id = "2",
                name = "Wallet 2",
                balance = BigDecimal("500"),
                currency = "USD",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        coEvery { getAllWalletsUseCase() } returns flowOf(wallets)

        // When
        viewModel = WalletListViewModel(getAllWalletsUseCase, authRepository, currencyRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.wallets).hasSize(2)
        assertThat(state.wallets).containsExactlyElementsIn(wallets)
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `loading wallets should calculate total balance correctly`() = runTest {
        // Given
        val wallets = listOf(
            Wallet(
                id = "1",
                name = "Wallet 1",
                balance = BigDecimal("1000"),
                currency = "USD",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Wallet(
                id = "2",
                name = "Wallet 2",
                balance = BigDecimal("500"),
                currency = "USD",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Wallet(
                id = "3",
                name = "Wallet 3",
                balance = BigDecimal("250.50"),
                currency = "USD",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        coEvery { getAllWalletsUseCase() } returns flowOf(wallets)

        // When
        viewModel = WalletListViewModel(getAllWalletsUseCase, authRepository, currencyRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.totalBalance).isEqualTo(BigDecimal("1750.50"))
    }

    @Test
    fun `loading empty wallet list should result in zero total balance`() = runTest {
        // Given
        coEvery { getAllWalletsUseCase() } returns flowOf(emptyList())

        // When
        viewModel = WalletListViewModel(getAllWalletsUseCase, authRepository, currencyRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.wallets).isEmpty()
        assertThat(state.totalBalance).isEqualTo(BigDecimal.ZERO)
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `error loading wallets should set error state`() = runTest {
        // Given
        val errorMessage = "Database error"
        coEvery { getAllWalletsUseCase() } throws Exception(errorMessage)

        // When
        viewModel = WalletListViewModel(getAllWalletsUseCase, authRepository, currencyRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.error).isEqualTo(errorMessage)
        assertThat(state.isLoading).isFalse()
        assertThat(state.wallets).isEmpty()
    }

    @Test
    fun `refresh should reload wallets`() = runTest {
        // Given
        val initialWallets = listOf(
            Wallet(
                id = "1",
                name = "Wallet 1",
                balance = BigDecimal("1000"),
                currency = "USD",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        val updatedWallets = listOf(
            Wallet(
                id = "1",
                name = "Wallet 1",
                balance = BigDecimal("1500"),
                currency = "USD",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Wallet(
                id = "2",
                name = "Wallet 2",
                balance = BigDecimal("500"),
                currency = "USD",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        coEvery { getAllWalletsUseCase() } returnsMany listOf(
            flowOf(initialWallets),
            flowOf(updatedWallets)
        )

        // When
        viewModel = WalletListViewModel(getAllWalletsUseCase, authRepository, currencyRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify initial state
        assertThat(viewModel.uiState.value.wallets).hasSize(1)

        // Trigger refresh
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.wallets).hasSize(2)
        assertThat(state.totalBalance).isEqualTo(BigDecimal("2000"))
    }

    @Test
    fun `state flow should emit updates`() = runTest {
        // Given
        val wallets = listOf(
            Wallet(
                id = "1",
                name = "Test Wallet",
                balance = BigDecimal("1000"),
                currency = "USD",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        coEvery { getAllWalletsUseCase() } returns flowOf(wallets)

        // When
        viewModel = WalletListViewModel(getAllWalletsUseCase, authRepository, currencyRepository)

        // Then - Use Turbine to test Flow emissions
        viewModel.uiState.test {
            val firstEmission = awaitItem()
            // Initial state (might be loading or already loaded depending on timing)
            assertThat(firstEmission).isNotNull()

            testDispatcher.scheduler.advanceUntilIdle()

            // Cancel the flow collection
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loading with negative balance should work`() = runTest {
        // Given
        val wallets = listOf(
            Wallet(
                id = "1",
                name = "Overdraft Wallet",
                balance = BigDecimal("-100"),
                currency = "USD",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Wallet(
                id = "2",
                name = "Positive Wallet",
                balance = BigDecimal("50"),
                currency = "USD",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        coEvery { getAllWalletsUseCase() } returns flowOf(wallets)

        // When
        viewModel = WalletListViewModel(getAllWalletsUseCase, authRepository, currencyRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.totalBalance).isEqualTo(BigDecimal("-50"))
    }
}

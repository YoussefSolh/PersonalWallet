package com.youssefsolh.personalwallet.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.usecase.CreateWalletUseCase
import com.youssefsolh.personalwallet.domain.usecase.GetWalletByIdUseCase
import com.youssefsolh.personalwallet.domain.usecase.UpdateWalletUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * Unit tests for AddWalletViewModel
 * Tests wallet creation, editing, and state management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddWalletViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AddWalletViewModel
    private val createWalletUseCase: CreateWalletUseCase = mockk()
    private val getWalletByIdUseCase: GetWalletByIdUseCase = mockk()
    private val updateWalletUseCase: UpdateWalletUseCase = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AddWalletViewModel(
            createWalletUseCase = createWalletUseCase,
            getWalletByIdUseCase = getWalletByIdUseCase,
            updateWalletUseCase = updateWalletUseCase
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
        assertThat(state.isLoading).isFalse()
        assertThat(state.isSuccess).isFalse()
        assertThat(state.error).isNull()
        assertThat(state.wallet).isNull()
    }

    @Test
    fun `creating wallet with valid data should succeed`() = runTest {
        // Given
        val name = "My Wallet"
        val initialBalance = 1000.0
        val currency = "USD"

        coEvery {
            createWalletUseCase(name, BigDecimal.valueOf(initialBalance), currency)
        } returns Result.success(Unit)

        // When
        viewModel.createWallet(name, initialBalance, currency)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.isSuccess).isTrue()
        assertThat(state.error).isNull()

        coVerify {
            createWalletUseCase(name, BigDecimal.valueOf(initialBalance), currency)
        }
    }

    @Test
    fun `creating wallet with zero balance should succeed`() = runTest {
        // Given
        val name = "Empty Wallet"
        val initialBalance = 0.0
        val currency = "USD"

        coEvery {
            createWalletUseCase(name, BigDecimal.ZERO, currency)
        } returns Result.success(Unit)

        // When
        viewModel.createWallet(name, initialBalance, currency)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isSuccess).isTrue()
        assertThat(state.error).isNull()
    }

    @Test
    fun `creating wallet with error should set error state`() = runTest {
        // Given
        val name = "Test Wallet"
        val initialBalance = 500.0
        val currency = "USD"
        val errorMessage = "Database error"

        coEvery {
            createWalletUseCase(name, BigDecimal.valueOf(initialBalance), currency)
        } throws Exception(errorMessage)

        // When
        viewModel.createWallet(name, initialBalance, currency)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.isSuccess).isFalse()
        assertThat(state.error).isEqualTo(errorMessage)
    }

    @Test
    fun `loading wallet by id should populate state`() = runTest {
        // Given
        val walletId = "wallet1"
        val wallet = Wallet(
            id = walletId,
            name = "Test Wallet",
            balance = BigDecimal("1000"),
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { getWalletByIdUseCase(walletId) } returns wallet

        // When
        viewModel.loadWallet(walletId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.wallet).isEqualTo(wallet)
        assertThat(state.error).isNull()
    }

    @Test
    fun `loading non-existent wallet should set error state`() = runTest {
        // Given
        val walletId = "nonexistent"
        val errorMessage = "Wallet not found"

        coEvery { getWalletByIdUseCase(walletId) } throws Exception(errorMessage)

        // When
        viewModel.loadWallet(walletId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.wallet).isNull()
        assertThat(state.error).isEqualTo(errorMessage)
    }

    @Test
    fun `updating wallet with valid data should succeed`() = runTest {
        // Given
        val walletId = "wallet1"
        val name = "Updated Wallet"
        val balance = 1500.0
        val currency = "EUR"

        coEvery {
            updateWalletUseCase(match {
                it.id == walletId &&
                it.name == name &&
                it.balance == BigDecimal.valueOf(balance) &&
                it.currency == currency
            })
        } returns Result.success(Unit)

        // When
        viewModel.updateWallet(walletId, name, balance, currency)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.isSuccess).isTrue()
        assertThat(state.error).isNull()

        coVerify {
            updateWalletUseCase(match {
                it.id == walletId &&
                it.name == name &&
                it.balance == BigDecimal.valueOf(balance) &&
                it.currency == currency
            })
        }
    }

    @Test
    fun `updating wallet with error should set error state`() = runTest {
        // Given
        val walletId = "wallet1"
        val name = "Test Wallet"
        val balance = 1000.0
        val currency = "USD"
        val errorMessage = "Update failed"

        coEvery {
            updateWalletUseCase(any())
        } throws Exception(errorMessage)

        // When
        viewModel.updateWallet(walletId, name, balance, currency)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.isSuccess).isFalse()
        assertThat(state.error).isEqualTo(errorMessage)
    }

    @Test
    fun `creating wallet should set loading state during operation`() = runTest {
        // Given
        val name = "Test Wallet"
        val initialBalance = 500.0
        val currency = "USD"

        coEvery {
            createWalletUseCase(any(), any(), any())
        } coAnswers {
            // State should be loading at this point
            assertThat(viewModel.uiState.value.isLoading).isTrue()
            Result.success(Unit)
        }

        // When
        viewModel.createWallet(name, initialBalance, currency)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - loading should be false after completion
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `updating wallet with different currency should succeed`() = runTest {
        // Given
        val walletId = "wallet1"
        val name = "Euro Wallet"
        val balance = 2000.0
        val currency = "EUR"

        coEvery {
            updateWalletUseCase(match { it.currency == "EUR" })
        } returns Result.success(Unit)

        // When
        viewModel.updateWallet(walletId, name, balance, currency)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isSuccess).isTrue()
        assertThat(state.error).isNull()
    }
}

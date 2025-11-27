package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for CreateWalletUseCase
 */
class CreateWalletUseCaseTest {

    private lateinit var createWalletUseCase: CreateWalletUseCase
    private val walletRepository: WalletRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        createWalletUseCase = CreateWalletUseCase(walletRepository)
    }

    @Test
    fun `creating wallet with valid data should succeed`() = runTest {
        // Given
        val wallet = Wallet(
            id = "wallet1",
            name = "Test Wallet",
            balance = BigDecimal("1000"),
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { walletRepository.insertWallet(wallet) } returns Unit

        // When
        val result = createWalletUseCase(wallet)

        // Then
        assertTrue(result.isSuccess)
        coVerify { walletRepository.insertWallet(wallet) }
    }

    @Test
    fun `creating wallet with zero balance should succeed`() = runTest {
        // Given
        val wallet = Wallet(
            id = "wallet1",
            name = "Empty Wallet",
            balance = BigDecimal.ZERO,
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { walletRepository.insertWallet(wallet) } returns Unit

        // When
        val result = createWalletUseCase(wallet)

        // Then
        assertTrue(result.isSuccess)
        coVerify { walletRepository.insertWallet(wallet) }
    }

    @Test
    fun `creating wallet with different currency should succeed`() = runTest {
        // Given
        val wallet = Wallet(
            id = "wallet1",
            name = "Euro Wallet",
            balance = BigDecimal("500"),
            currency = "EUR",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { walletRepository.insertWallet(wallet) } returns Unit

        // When
        val result = createWalletUseCase(wallet)

        // Then
        assertTrue(result.isSuccess)
        coVerify { walletRepository.insertWallet(wallet) }
    }

    @Test
    fun `creating wallet with repository error should return failure`() = runTest {
        // Given
        val wallet = Wallet(
            id = "wallet1",
            name = "Test Wallet",
            balance = BigDecimal("1000"),
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { walletRepository.insertWallet(wallet) } throws Exception("Database error")

        // When
        val result = createWalletUseCase(wallet)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `creating wallet with large balance should succeed`() = runTest {
        // Given
        val wallet = Wallet(
            id = "wallet1",
            name = "Large Balance Wallet",
            balance = BigDecimal("1000000.99"),
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { walletRepository.insertWallet(wallet) } returns Unit

        // When
        val result = createWalletUseCase(wallet)

        // Then
        assertTrue(result.isSuccess)
        coVerify { walletRepository.insertWallet(wallet) }
    }
}

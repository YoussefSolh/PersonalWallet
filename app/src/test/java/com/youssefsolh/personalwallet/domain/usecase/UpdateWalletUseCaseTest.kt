package com.youssefsolh.personalwallet.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for UpdateWalletUseCase
 * Tests wallet update functionality
 */
class UpdateWalletUseCaseTest {

    private lateinit var updateWalletUseCase: UpdateWalletUseCase
    private val walletRepository: WalletRepository = mockk()

    private val testWallet = Wallet(
        id = "wallet1",
        name = "Test Wallet",
        balance = BigDecimal("1000"),
        currency = "USD",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        updateWalletUseCase = UpdateWalletUseCase(walletRepository)
    }

    @Test
    fun `invoke should call repository updateWallet with correct wallet`() = runTest {
        // Given
        coEvery { walletRepository.updateWallet(testWallet) } returns Unit

        // When
        updateWalletUseCase(testWallet)

        // Then
        coVerify(exactly = 1) { walletRepository.updateWallet(testWallet) }
    }

    @Test
    fun `invoke should update wallet name`() = runTest {
        // Given
        val updatedWallet = testWallet.copy(name = "Updated Wallet")
        coEvery { walletRepository.updateWallet(any()) } returns Unit

        // When
        updateWalletUseCase(updatedWallet)

        // Then
        coVerify {
            walletRepository.updateWallet(
                match { it.name == "Updated Wallet" && it.id == "wallet1" }
            )
        }
    }

    @Test
    fun `invoke should update wallet balance`() = runTest {
        // Given
        val updatedWallet = testWallet.copy(balance = BigDecimal("2000"))
        coEvery { walletRepository.updateWallet(any()) } returns Unit

        // When
        updateWalletUseCase(updatedWallet)

        // Then
        coVerify {
            walletRepository.updateWallet(
                match { it.balance == BigDecimal("2000") && it.id == "wallet1" }
            )
        }
    }

    @Test
    fun `invoke should update wallet currency`() = runTest {
        // Given
        val updatedWallet = testWallet.copy(currency = "EUR")
        coEvery { walletRepository.updateWallet(any()) } returns Unit

        // When
        updateWalletUseCase(updatedWallet)

        // Then
        coVerify {
            walletRepository.updateWallet(
                match { it.currency == "EUR" && it.id == "wallet1" }
            )
        }
    }

    @Test
    fun `invoke should handle wallet with zero balance`() = runTest {
        // Given
        val zeroWallet = testWallet.copy(balance = BigDecimal.ZERO)
        coEvery { walletRepository.updateWallet(any()) } returns Unit

        // When
        updateWalletUseCase(zeroWallet)

        // Then
        coVerify {
            walletRepository.updateWallet(
                match { it.balance == BigDecimal.ZERO }
            )
        }
    }

    @Test
    fun `invoke should handle wallet with negative balance`() = runTest {
        // Given
        val negativeWallet = testWallet.copy(balance = BigDecimal("-500.50"))
        coEvery { walletRepository.updateWallet(any()) } returns Unit

        // When
        updateWalletUseCase(negativeWallet)

        // Then
        coVerify {
            walletRepository.updateWallet(
                match { it.balance == BigDecimal("-500.50") }
            )
        }
    }

    @Test
    fun `invoke should handle wallet with large balance`() = runTest {
        // Given
        val largeWallet = testWallet.copy(balance = BigDecimal("9999999.99"))
        coEvery { walletRepository.updateWallet(any()) } returns Unit

        // When
        updateWalletUseCase(largeWallet)

        // Then
        coVerify {
            walletRepository.updateWallet(
                match { it.balance == BigDecimal("9999999.99") }
            )
        }
    }

    @Test
    fun `invoke should update multiple fields simultaneously`() = runTest {
        // Given
        val updatedWallet = testWallet.copy(
            name = "New Name",
            balance = BigDecimal("3000"),
            currency = "GBP"
        )
        coEvery { walletRepository.updateWallet(any()) } returns Unit

        // When
        updateWalletUseCase(updatedWallet)

        // Then
        coVerify {
            walletRepository.updateWallet(
                match {
                    it.id == "wallet1" &&
                    it.name == "New Name" &&
                    it.balance == BigDecimal("3000") &&
                    it.currency == "GBP"
                }
            )
        }
    }

    @Test
    fun `invoke should preserve wallet id during update`() = runTest {
        // Given
        val updatedWallet = testWallet.copy(
            name = "Changed Name",
            balance = BigDecimal("5000")
        )
        coEvery { walletRepository.updateWallet(any()) } returns Unit

        // When
        updateWalletUseCase(updatedWallet)

        // Then
        coVerify {
            walletRepository.updateWallet(
                match { it.id == "wallet1" }
            )
        }
    }

    @Test
    fun `invoke should handle repository exceptions`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { walletRepository.updateWallet(any()) } throws exception

        // When/Then
        try {
            updateWalletUseCase(testWallet)
            assertThat(false).isTrue() // Should not reach here
        } catch (e: RuntimeException) {
            assertThat(e.message).isEqualTo("Database error")
        }
    }

    @Test
    fun `invoke should be called multiple times for different wallets`() = runTest {
        // Given
        val wallet1 = Wallet(id = "w1", name = "Wallet 1", balance = BigDecimal("100"), currency = "USD", createdAt = 1L, updatedAt = 1L)
        val wallet2 = Wallet(id = "w2", name = "Wallet 2", balance = BigDecimal("200"), currency = "EUR", createdAt = 1L, updatedAt = 1L)
        val wallet3 = Wallet(id = "w3", name = "Wallet 3", balance = BigDecimal("300"), currency = "GBP", createdAt = 1L, updatedAt = 1L)

        coEvery { walletRepository.updateWallet(any()) } returns Unit

        // When
        updateWalletUseCase(wallet1)
        updateWalletUseCase(wallet2)
        updateWalletUseCase(wallet3)

        // Then
        coVerify(exactly = 3) { walletRepository.updateWallet(any()) }
        coVerify { walletRepository.updateWallet(wallet1) }
        coVerify { walletRepository.updateWallet(wallet2) }
        coVerify { walletRepository.updateWallet(wallet3) }
    }
}

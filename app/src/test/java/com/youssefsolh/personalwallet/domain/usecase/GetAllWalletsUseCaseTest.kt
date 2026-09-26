package com.youssefsolh.personalwallet.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for GetAllWalletsUseCase
 * Tests wallet retrieval functionality
 */
class GetAllWalletsUseCaseTest {

    private lateinit var getAllWalletsUseCase: GetAllWalletsUseCase
    private val walletRepository: WalletRepository = mockk()

    @Before
    fun setup() {
        getAllWalletsUseCase = GetAllWalletsUseCase(walletRepository)
    }

    @Test
    fun `invoke should return flow of wallets from repository`() = runTest {
        // Given
        val wallets = listOf(
            Wallet(
                id = "wallet1",
                name = "Wallet 1",
                balance = BigDecimal("1000"),
                currency = "USD",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Wallet(
                id = "wallet2",
                name = "Wallet 2",
                balance = BigDecimal("500"),
                currency = "EUR",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        coEvery { walletRepository.getAllWallets() } returns flowOf(wallets)

        // When
        getAllWalletsUseCase().test {
            val result = awaitItem()

            // Then
            assertThat(result).hasSize(2)
            assertThat(result[0].id).isEqualTo("wallet1")
            assertThat(result[0].name).isEqualTo("Wallet 1")
            assertThat(result[0].balance).isEqualTo(BigDecimal("1000"))
            assertThat(result[0].currency).isEqualTo("USD")
            assertThat(result[1].id).isEqualTo("wallet2")
            assertThat(result[1].name).isEqualTo("Wallet 2")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should return empty list when no wallets exist`() = runTest {
        // Given
        coEvery { walletRepository.getAllWallets() } returns flowOf(emptyList())

        // When
        getAllWalletsUseCase().test {
            val result = awaitItem()

            // Then
            assertThat(result).isEmpty()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should handle single wallet`() = runTest {
        // Given
        val wallet = Wallet(
            id = "wallet1",
            name = "My Wallet",
            balance = BigDecimal("2500.50"),
            currency = "GBP",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        coEvery { walletRepository.getAllWallets() } returns flowOf(listOf(wallet))

        // When
        getAllWalletsUseCase().test {
            val result = awaitItem()

            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0]).isEqualTo(wallet)
            assertThat(result[0].balance).isEqualTo(BigDecimal("2500.50"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should return wallets with different currencies`() = runTest {
        // Given
        val wallets = listOf(
            Wallet(id = "w1", name = "USD Wallet", balance = BigDecimal("1000"), currency = "USD", createdAt = 1L, updatedAt = 1L),
            Wallet(id = "w2", name = "EUR Wallet", balance = BigDecimal("500"), currency = "EUR", createdAt = 1L, updatedAt = 1L),
            Wallet(id = "w3", name = "GBP Wallet", balance = BigDecimal("750"), currency = "GBP", createdAt = 1L, updatedAt = 1L)
        )
        coEvery { walletRepository.getAllWallets() } returns flowOf(wallets)

        // When
        getAllWalletsUseCase().test {
            val result = awaitItem()

            // Then
            assertThat(result).hasSize(3)
            assertThat(result.map { it.currency }).containsExactly("USD", "EUR", "GBP")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should handle wallets with zero and negative balances`() = runTest {
        // Given
        val wallets = listOf(
            Wallet(id = "w1", name = "Zero Wallet", balance = BigDecimal.ZERO, currency = "USD", createdAt = 1L, updatedAt = 1L),
            Wallet(id = "w2", name = "Negative Wallet", balance = BigDecimal("-100"), currency = "USD", createdAt = 1L, updatedAt = 1L),
            Wallet(id = "w3", name = "Positive Wallet", balance = BigDecimal("500"), currency = "USD", createdAt = 1L, updatedAt = 1L)
        )
        coEvery { walletRepository.getAllWallets() } returns flowOf(wallets)

        // When
        getAllWalletsUseCase().test {
            val result = awaitItem()

            // Then
            assertThat(result).hasSize(3)
            assertThat(result[0].balance).isEqualTo(BigDecimal.ZERO)
            assertThat(result[1].balance).isEqualTo(BigDecimal("-100"))
            assertThat(result[2].balance).isEqualTo(BigDecimal("500"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should emit multiple updates when repository emits`() = runTest {
        // Given - Simulate repository emitting updates
        val initialWallets = listOf(
            Wallet(id = "w1", name = "Wallet 1", balance = BigDecimal("1000"), currency = "USD", createdAt = 1L, updatedAt = 1L)
        )
        val updatedWallets = listOf(
            Wallet(id = "w1", name = "Wallet 1", balance = BigDecimal("1500"), currency = "USD", createdAt = 1L, updatedAt = 2L),
            Wallet(id = "w2", name = "Wallet 2", balance = BigDecimal("500"), currency = "USD", createdAt = 1L, updatedAt = 1L)
        )

        coEvery { walletRepository.getAllWallets() } returns flowOf(initialWallets, updatedWallets)

        // When
        getAllWalletsUseCase().test {
            // First emission
            val first = awaitItem()
            assertThat(first).hasSize(1)
            assertThat(first[0].balance).isEqualTo(BigDecimal("1000"))

            // Second emission
            val second = awaitItem()
            assertThat(second).hasSize(2)
            assertThat(second[0].balance).isEqualTo(BigDecimal("1500"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should preserve wallet ordering from repository`() = runTest {
        // Given
        val wallets = listOf(
            Wallet(id = "w3", name = "Third", balance = BigDecimal("300"), currency = "USD", createdAt = 3L, updatedAt = 3L),
            Wallet(id = "w1", name = "First", balance = BigDecimal("100"), currency = "USD", createdAt = 1L, updatedAt = 1L),
            Wallet(id = "w2", name = "Second", balance = BigDecimal("200"), currency = "USD", createdAt = 2L, updatedAt = 2L)
        )
        coEvery { walletRepository.getAllWallets() } returns flowOf(wallets)

        // When
        getAllWalletsUseCase().test {
            val result = awaitItem()

            // Then - Order should be preserved
            assertThat(result[0].name).isEqualTo("Third")
            assertThat(result[1].name).isEqualTo("First")
            assertThat(result[2].name).isEqualTo("Second")

            cancelAndIgnoreRemainingEvents()
        }
    }
}

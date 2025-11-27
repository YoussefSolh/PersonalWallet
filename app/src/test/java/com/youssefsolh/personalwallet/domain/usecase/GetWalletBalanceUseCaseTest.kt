package com.youssefsolh.personalwallet.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class GetWalletBalanceUseCaseTest {

    private lateinit var getWalletBalanceUseCase: GetWalletBalanceUseCase
    private val walletRepository: WalletRepository = mockk()

    @Before
    fun setup() {
        getWalletBalanceUseCase = GetWalletBalanceUseCase(walletRepository)
    }

    @Test
    fun `invoke should return wallet balance flow`() = runTest {
        // Given
        val walletId = "wallet1"
        val balance = BigDecimal("1000.50")
        coEvery { walletRepository.getWalletBalance(walletId) } returns flowOf(balance)

        // When
        getWalletBalanceUseCase(walletId).test {
            val result = awaitItem()

            // Then
            assertThat(result).isEqualTo(balance)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should handle zero balance`() = runTest {
        // Given
        val walletId = "wallet1"
        coEvery { walletRepository.getWalletBalance(walletId) } returns flowOf(BigDecimal.ZERO)

        // When
        getWalletBalanceUseCase(walletId).test {
            val result = awaitItem()

            // Then
            assertThat(result).isEqualTo(BigDecimal.ZERO)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should handle negative balance`() = runTest {
        // Given
        val walletId = "wallet1"
        val balance = BigDecimal("-500")
        coEvery { walletRepository.getWalletBalance(walletId) } returns flowOf(balance)

        // When
        getWalletBalanceUseCase(walletId).test {
            val result = awaitItem()

            // Then
            assertThat(result).isEqualTo(BigDecimal("-500"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should emit balance updates`() = runTest {
        // Given
        val walletId = "wallet1"
        coEvery { walletRepository.getWalletBalance(walletId) } returns flowOf(
            BigDecimal("100"),
            BigDecimal("200"),
            BigDecimal("300")
        )

        // When
        getWalletBalanceUseCase(walletId).test {
            assertThat(awaitItem()).isEqualTo(BigDecimal("100"))
            assertThat(awaitItem()).isEqualTo(BigDecimal("200"))
            assertThat(awaitItem()).isEqualTo(BigDecimal("300"))
            cancelAndIgnoreRemainingEvents()
        }
    }
}

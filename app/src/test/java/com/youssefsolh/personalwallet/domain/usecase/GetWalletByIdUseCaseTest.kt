package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for GetWalletByIdUseCase
 */
class GetWalletByIdUseCaseTest {

    private lateinit var getWalletByIdUseCase: GetWalletByIdUseCase
    private val walletRepository: WalletRepository = mockk()

    @Before
    fun setup() {
        getWalletByIdUseCase = GetWalletByIdUseCase(walletRepository)
    }

    @Test
    fun `getting existing wallet by id should return wallet`() = runTest {
        // Given
        val walletId = "wallet1"
        val expectedWallet = Wallet(
            id = walletId,
            name = "Test Wallet",
            balance = BigDecimal("1000"),
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { walletRepository.getWalletById(walletId) } returns expectedWallet

        // When
        val result = getWalletByIdUseCase(walletId)

        // Then
        assertThat(result).isEqualTo(expectedWallet)
        assertThat(result?.id).isEqualTo(walletId)
        assertThat(result?.name).isEqualTo("Test Wallet")
        assertThat(result?.balance).isEqualTo(BigDecimal("1000"))
    }

    @Test
    fun `getting non-existing wallet should return null`() = runTest {
        // Given
        val walletId = "nonexistent"
        coEvery { walletRepository.getWalletById(walletId) } returns null

        // When
        val result = getWalletByIdUseCase(walletId)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getting wallet with different currencies should work`() = runTest {
        // Given
        val eurWallet = Wallet(
            id = "eur-wallet",
            name = "Euro Wallet",
            balance = BigDecimal("500"),
            currency = "EUR",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { walletRepository.getWalletById("eur-wallet") } returns eurWallet

        // When
        val result = getWalletByIdUseCase("eur-wallet")

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.currency).isEqualTo("EUR")
    }

    @Test
    fun `getting wallet with zero balance should return wallet`() = runTest {
        // Given
        val zeroWallet = Wallet(
            id = "zero-wallet",
            name = "Empty Wallet",
            balance = BigDecimal.ZERO,
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { walletRepository.getWalletById("zero-wallet") } returns zeroWallet

        // When
        val result = getWalletByIdUseCase("zero-wallet")

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.balance).isEqualTo(BigDecimal.ZERO)
    }
}

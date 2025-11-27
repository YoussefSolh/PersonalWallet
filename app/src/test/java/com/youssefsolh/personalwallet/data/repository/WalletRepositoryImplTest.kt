package com.youssefsolh.personalwallet.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.data.local.dao.WalletDao
import com.youssefsolh.personalwallet.data.local.entity.WalletEntity
import com.youssefsolh.personalwallet.domain.model.Wallet
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for WalletRepositoryImpl
 * Tests data layer operations and entity-domain mapping
 */
class WalletRepositoryImplTest {

    private lateinit var repository: WalletRepositoryImpl
    private val walletDao: WalletDao = mockk()

    private val testWalletEntity = WalletEntity(
        id = "wallet1",
        name = "Test Wallet",
        balance = "1000.00",
        currency = "USD",
        createdAt = 1234567890L,
        updatedAt = 1234567890L,
        isDeleted = false
    )

    private val testWallet = Wallet(
        id = "wallet1",
        name = "Test Wallet",
        balance = BigDecimal("1000.00"),
        currency = "USD",
        createdAt = 1234567890L,
        updatedAt = 1234567890L
    )

    @Before
    fun setup() {
        repository = WalletRepositoryImpl(walletDao)
    }

    @Test
    fun `getAllWallets should return flow of domain wallets`() = runTest {
        // Given
        val entities = listOf(testWalletEntity)
        coEvery { walletDao.getAllWallets() } returns flowOf(entities)

        // When
        repository.getAllWallets().test {
            val wallets = awaitItem()

            // Then
            assertThat(wallets).hasSize(1)
            assertThat(wallets[0].id).isEqualTo("wallet1")
            assertThat(wallets[0].name).isEqualTo("Test Wallet")
            assertThat(wallets[0].balance).isEqualTo(BigDecimal("1000.00"))
            assertThat(wallets[0].currency).isEqualTo("USD")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllWallets should handle empty list`() = runTest {
        // Given
        coEvery { walletDao.getAllWallets() } returns flowOf(emptyList())

        // When
        repository.getAllWallets().test {
            val wallets = awaitItem()

            // Then
            assertThat(wallets).isEmpty()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getWalletById should return domain wallet when exists`() = runTest {
        // Given
        coEvery { walletDao.getWalletById("wallet1") } returns testWalletEntity

        // When
        val wallet = repository.getWalletById("wallet1")

        // Then
        assertThat(wallet).isNotNull()
        assertThat(wallet?.id).isEqualTo("wallet1")
        assertThat(wallet?.name).isEqualTo("Test Wallet")
        assertThat(wallet?.balance).isEqualTo(BigDecimal("1000.00"))
    }

    @Test
    fun `getWalletById should return null when wallet does not exist`() = runTest {
        // Given
        coEvery { walletDao.getWalletById("nonexistent") } returns null

        // When
        val wallet = repository.getWalletById("nonexistent")

        // Then
        assertThat(wallet).isNull()
    }

    @Test
    fun `insertWallet should convert domain to entity and call dao`() = runTest {
        // Given
        coEvery { walletDao.insertWallet(any()) } returns Unit

        // When
        repository.insertWallet(testWallet)

        // Then
        coVerify {
            walletDao.insertWallet(
                match {
                    it.id == "wallet1" &&
                    it.name == "Test Wallet" &&
                    it.balance == "1000.00" &&
                    it.currency == "USD"
                }
            )
        }
    }

    @Test
    fun `updateWallet should convert domain to entity and call dao`() = runTest {
        // Given
        coEvery { walletDao.updateWallet(any()) } returns Unit

        // When
        repository.updateWallet(testWallet)

        // Then
        coVerify {
            walletDao.updateWallet(
                match {
                    it.id == "wallet1" &&
                    it.name == "Test Wallet" &&
                    it.balance == "1000.00" &&
                    it.currency == "USD"
                }
            )
        }
    }

    @Test
    fun `deleteWallet should call dao with wallet id`() = runTest {
        // Given
        coEvery { walletDao.deleteWallet("wallet1") } returns Unit

        // When
        repository.deleteWallet("wallet1")

        // Then
        coVerify { walletDao.deleteWallet("wallet1") }
    }

    @Test
    fun `getWalletBalance should return flow of BigDecimal`() = runTest {
        // Given
        coEvery { walletDao.getWalletBalance("wallet1") } returns flowOf("1500.50")

        // When
        repository.getWalletBalance("wallet1").test {
            val balance = awaitItem()

            // Then
            assertThat(balance).isEqualTo(BigDecimal("1500.50"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllWallets should map multiple entities correctly`() = runTest {
        // Given
        val entities = listOf(
            WalletEntity("w1", "Wallet 1", "100", "USD", 1L, 1L, false),
            WalletEntity("w2", "Wallet 2", "200", "EUR", 2L, 2L, false),
            WalletEntity("w3", "Wallet 3", "300.50", "GBP", 3L, 3L, false)
        )
        coEvery { walletDao.getAllWallets() } returns flowOf(entities)

        // When
        repository.getAllWallets().test {
            val wallets = awaitItem()

            // Then
            assertThat(wallets).hasSize(3)
            assertThat(wallets[0].balance).isEqualTo(BigDecimal("100"))
            assertThat(wallets[1].balance).isEqualTo(BigDecimal("200"))
            assertThat(wallets[2].balance).isEqualTo(BigDecimal("300.50"))
            assertThat(wallets[0].currency).isEqualTo("USD")
            assertThat(wallets[1].currency).isEqualTo("EUR")
            assertThat(wallets[2].currency).isEqualTo("GBP")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertWallet with zero balance should work`() = runTest {
        // Given
        val zeroWallet = testWallet.copy(balance = BigDecimal.ZERO)
        coEvery { walletDao.insertWallet(any()) } returns Unit

        // When
        repository.insertWallet(zeroWallet)

        // Then
        coVerify {
            walletDao.insertWallet(
                match { it.balance == "0" }
            )
        }
    }

    @Test
    fun `insertWallet with large balance should preserve precision`() = runTest {
        // Given
        val largeWallet = testWallet.copy(balance = BigDecimal("9999999.99"))
        coEvery { walletDao.insertWallet(any()) } returns Unit

        // When
        repository.insertWallet(largeWallet)

        // Then
        coVerify {
            walletDao.insertWallet(
                match { it.balance == "9999999.99" }
            )
        }
    }

    @Test
    fun `updateWallet should preserve timestamps`() = runTest {
        // Given
        val updatedWallet = testWallet.copy(
            name = "Updated Name",
            balance = BigDecimal("2000")
        )
        coEvery { walletDao.updateWallet(any()) } returns Unit

        // When
        repository.updateWallet(updatedWallet)

        // Then
        coVerify {
            walletDao.updateWallet(
                match {
                    it.name == "Updated Name" &&
                    it.balance == "2000" &&
                    it.createdAt == 1234567890L
                }
            )
        }
    }

    @Test
    fun `getWalletBalance should handle zero balance`() = runTest {
        // Given
        coEvery { walletDao.getWalletBalance("wallet1") } returns flowOf("0")

        // When
        repository.getWalletBalance("wallet1").test {
            val balance = awaitItem()

            // Then
            assertThat(balance).isEqualTo(BigDecimal.ZERO)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getWalletBalance should handle negative balance`() = runTest {
        // Given
        coEvery { walletDao.getWalletBalance("wallet1") } returns flowOf("-50.25")

        // When
        repository.getWalletBalance("wallet1").test {
            val balance = awaitItem()

            // Then
            assertThat(balance).isEqualTo(BigDecimal("-50.25"))

            cancelAndIgnoreRemainingEvents()
        }
    }
}

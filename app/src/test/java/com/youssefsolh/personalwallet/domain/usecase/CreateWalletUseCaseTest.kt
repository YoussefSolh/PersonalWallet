package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
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
    fun `creating wallet inserts it with the given fields`() = runTest {
        val inserted = slot<Wallet>()
        coEvery { walletRepository.insertWallet(capture(inserted)) } returns Unit

        createWalletUseCase("Test Wallet", BigDecimal("1000.99"), "EUR")

        assertEquals("Test Wallet", inserted.captured.name)
        assertEquals(BigDecimal("1000.99"), inserted.captured.balance)
        assertEquals("EUR", inserted.captured.currency)
        assertTrue(inserted.captured.id.isNotBlank())
    }

    @Test
    fun `each wallet gets a unique id`() = runTest {
        val ids = mutableListOf<String>()
        coEvery { walletRepository.insertWallet(any()) } answers { ids += firstArg<Wallet>().id }

        createWalletUseCase("A", BigDecimal.ZERO, "USD")
        createWalletUseCase("B", BigDecimal.ZERO, "USD")

        assertNotEquals(ids[0], ids[1])
    }

    @Test(expected = IllegalStateException::class)
    fun `repository failure propagates`() = runTest {
        coEvery { walletRepository.insertWallet(any()) } throws IllegalStateException("Database error")
        createWalletUseCase("Test", BigDecimal.ONE, "USD")
    }

    @Test
    fun `zero and negative initial balances are accepted`() = runTest {
        createWalletUseCase("Zero", BigDecimal.ZERO, "USD")
        createWalletUseCase("Overdrawn", BigDecimal("-50"), "USD")
        coVerify(exactly = 2) { walletRepository.insertWallet(any()) }
    }
}

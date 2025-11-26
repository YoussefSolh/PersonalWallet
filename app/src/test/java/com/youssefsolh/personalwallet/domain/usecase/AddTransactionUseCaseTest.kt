package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.math.BigDecimal

class AddTransactionUseCaseTest {

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var walletRepository: WalletRepository

    private lateinit var addTransactionUseCase: AddTransactionUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        addTransactionUseCase = AddTransactionUseCase(transactionRepository, walletRepository)
    }

    @Test
    fun `adding income transaction increases wallet balance`() = runTest {
        // Given
        val wallet = Wallet(
            id = "wallet1",
            name = "Test Wallet",
            balance = BigDecimal("100"),
            currency = "USD"
        )
        val transaction = Transaction(
            id = "tx1",
            amount = BigDecimal("50"),
            type = TransactionType.INCOME,
            description = "Salary",
            categoryId = "cat1",
            fromWalletId = null,
            toWalletId = "wallet1"
        )

        `when`(walletRepository.getWalletById("wallet1")).thenReturn(wallet)

        // When
        val result = addTransactionUseCase(transaction)

        // Then
        assertTrue(result.isSuccess)
        verify(transactionRepository).insertTransaction(transaction)
        verify(walletRepository).updateWallet(
            argThat { it.balance == BigDecimal("150") }
        )
    }

    @Test
    fun `adding expense transaction decreases wallet balance`() = runTest {
        // Given
        val wallet = Wallet(
            id = "wallet1",
            name = "Test Wallet",
            balance = BigDecimal("100"),
            currency = "USD"
        )
        val transaction = Transaction(
            id = "tx1",
            amount = BigDecimal("30"),
            type = TransactionType.EXPENSE,
            description = "Groceries",
            categoryId = "cat1",
            fromWalletId = "wallet1",
            toWalletId = null
        )

        `when`(walletRepository.getWalletById("wallet1")).thenReturn(wallet)

        // When
        val result = addTransactionUseCase(transaction)

        // Then
        assertTrue(result.isSuccess)
        verify(walletRepository).updateWallet(
            argThat { it.balance == BigDecimal("70") }
        )
    }

    @Test
    fun `transfer transaction updates both wallet balances`() = runTest {
        // Given
        val fromWallet = Wallet(
            id = "wallet1",
            name = "Wallet 1",
            balance = BigDecimal("100"),
            currency = "USD"
        )
        val toWallet = Wallet(
            id = "wallet2",
            name = "Wallet 2",
            balance = BigDecimal("50"),
            currency = "USD"
        )
        val transaction = Transaction(
            id = "tx1",
            amount = BigDecimal("25"),
            type = TransactionType.TRANSFER,
            description = "Transfer",
            categoryId = "transfer",
            fromWalletId = "wallet1",
            toWalletId = "wallet2"
        )

        `when`(walletRepository.getWalletById("wallet1")).thenReturn(fromWallet)
        `when`(walletRepository.getWalletById("wallet2")).thenReturn(toWallet)

        // When
        val result = addTransactionUseCase(transaction)

        // Then
        assertTrue(result.isSuccess)
        verify(walletRepository).updateWallet(
            argThat { it.id == "wallet1" && it.balance == BigDecimal("75") }
        )
        verify(walletRepository).updateWallet(
            argThat { it.id == "wallet2" && it.balance == BigDecimal("75") }
        )
    }
}

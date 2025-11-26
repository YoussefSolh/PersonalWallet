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

class DeleteTransactionUseCaseTest {

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var walletRepository: WalletRepository

    private lateinit var deleteTransactionUseCase: DeleteTransactionUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        deleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository, walletRepository)
    }

    @Test
    fun `deleting income transaction reverses wallet balance`() = runTest {
        // Given
        val wallet = Wallet(
            id = "wallet1",
            name = "Test Wallet",
            balance = BigDecimal("150"),
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
        val result = deleteTransactionUseCase(transaction)

        // Then
        assertTrue(result.isSuccess)
        verify(walletRepository).updateWallet(
            argThat { it.balance == BigDecimal("100") }
        )
        verify(transactionRepository).updateTransaction(
            argThat { it.isDeleted }
        )
    }

    @Test
    fun `deleting expense transaction restores wallet balance`() = runTest {
        // Given
        val wallet = Wallet(
            id = "wallet1",
            name = "Test Wallet",
            balance = BigDecimal("70"),
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
        val result = deleteTransactionUseCase(transaction)

        // Then
        assertTrue(result.isSuccess)
        verify(walletRepository).updateWallet(
            argThat { it.balance == BigDecimal("100") }
        )
    }
}

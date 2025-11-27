package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
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
 * Unit tests for UpdateTransactionUseCase
 * Tests the business logic of updating transactions and wallet balance adjustments
 */
class UpdateTransactionUseCaseTest {

    private lateinit var updateTransactionUseCase: UpdateTransactionUseCase
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val walletRepository: WalletRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        updateTransactionUseCase = UpdateTransactionUseCase(
            transactionRepository = transactionRepository,
            walletRepository = walletRepository
        )
    }

    @Test
    fun `update expense amount should adjust wallet balance correctly`() = runTest {
        // Given
        val wallet = Wallet(
            id = "wallet1",
            name = "Test Wallet",
            balance = BigDecimal("1000"),
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val oldTransaction = Transaction(
            id = "tx1",
            amount = BigDecimal("50"),
            type = TransactionType.EXPENSE,
            description = "Old Expense",
            categoryId = "cat1",
            fromWalletId = "wallet1",
            toWalletId = null,
            timestamp = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newTransaction = oldTransaction.copy(
            amount = BigDecimal("100"),
            description = "Updated Expense"
        )

        coEvery { walletRepository.getWalletById("wallet1") } returns wallet

        // When
        val result = updateTransactionUseCase(oldTransaction, newTransaction)

        // Then
        assertTrue(result.isSuccess)

        // Verify wallet balance was updated: 1000 + 50 (reverse old) - 100 (apply new) = 950
        coVerify {
            walletRepository.updateWallet(
                match { it.balance == BigDecimal("950") }
            )
        }

        // Verify transaction was updated
        coVerify {
            transactionRepository.updateTransaction(newTransaction)
        }
    }

    @Test
    fun `update income amount should adjust wallet balance correctly`() = runTest {
        // Given
        val wallet = Wallet(
            id = "wallet1",
            name = "Test Wallet",
            balance = BigDecimal("1000"),
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val oldTransaction = Transaction(
            id = "tx1",
            amount = BigDecimal("200"),
            type = TransactionType.INCOME,
            description = "Old Income",
            categoryId = "cat1",
            fromWalletId = null,
            toWalletId = "wallet1",
            timestamp = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newTransaction = oldTransaction.copy(
            amount = BigDecimal("300"),
            description = "Updated Income"
        )

        coEvery { walletRepository.getWalletById("wallet1") } returns wallet

        // When
        val result = updateTransactionUseCase(oldTransaction, newTransaction)

        // Then
        assertTrue(result.isSuccess)

        // Verify wallet balance: 1000 - 200 (reverse old) + 300 (apply new) = 1100
        coVerify {
            walletRepository.updateWallet(
                match { it.balance == BigDecimal("1100") }
            )
        }
    }

    @Test
    fun `changing transaction type from expense to income should adjust balance correctly`() = runTest {
        // Given
        val wallet = Wallet(
            id = "wallet1",
            name = "Test Wallet",
            balance = BigDecimal("1000"),
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val oldTransaction = Transaction(
            id = "tx1",
            amount = BigDecimal("50"),
            type = TransactionType.EXPENSE,
            description = "Was Expense",
            categoryId = "cat1",
            fromWalletId = "wallet1",
            toWalletId = null,
            timestamp = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newTransaction = oldTransaction.copy(
            type = TransactionType.INCOME,
            fromWalletId = null,
            toWalletId = "wallet1"
        )

        coEvery { walletRepository.getWalletById("wallet1") } returns wallet

        // When
        val result = updateTransactionUseCase(oldTransaction, newTransaction)

        // Then
        assertTrue(result.isSuccess)

        // Reverse expense (+50) then apply income (+50) = 1000 + 50 + 50 = 1100
        coVerify {
            walletRepository.updateWallet(
                match { it.balance == BigDecimal("1100") }
            )
        }
    }

    @Test
    fun `update transfer should adjust both wallet balances correctly`() = runTest {
        // Given
        val fromWallet = Wallet(
            id = "wallet1",
            name = "From Wallet",
            balance = BigDecimal("1000"),
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val toWallet = Wallet(
            id = "wallet2",
            name = "To Wallet",
            balance = BigDecimal("500"),
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val oldTransaction = Transaction(
            id = "tx1",
            amount = BigDecimal("100"),
            type = TransactionType.TRANSFER,
            description = "Transfer",
            categoryId = "cat1",
            fromWalletId = "wallet1",
            toWalletId = "wallet2",
            timestamp = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newTransaction = oldTransaction.copy(
            amount = BigDecimal("200")
        )

        coEvery { walletRepository.getWalletById("wallet1") } returns fromWallet
        coEvery { walletRepository.getWalletById("wallet2") } returns toWallet

        // When
        val result = updateTransactionUseCase(oldTransaction, newTransaction)

        // Then
        assertTrue(result.isSuccess)

        // From wallet: 1000 + 100 (reverse old transfer) - 200 (new transfer) = 900
        coVerify {
            walletRepository.updateWallet(
                match { it.id == "wallet1" && it.balance == BigDecimal("900") }
            )
        }

        // To wallet: 500 - 100 (reverse old) + 200 (new) = 600
        coVerify {
            walletRepository.updateWallet(
                match { it.id == "wallet2" && it.balance == BigDecimal("600") }
            )
        }
    }

    @Test
    fun `update transaction with missing wallet should return failure`() = runTest {
        // Given
        val oldTransaction = Transaction(
            id = "tx1",
            amount = BigDecimal("50"),
            type = TransactionType.EXPENSE,
            description = "Expense",
            categoryId = "cat1",
            fromWalletId = "wallet1",
            toWalletId = null,
            timestamp = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newTransaction = oldTransaction.copy(amount = BigDecimal("100"))

        coEvery { walletRepository.getWalletById("wallet1") } returns null

        // When
        val result = updateTransactionUseCase(oldTransaction, newTransaction)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `changing transfer destination wallet should update both old and new wallets`() = runTest {
        // Given
        val fromWallet = Wallet(
            id = "wallet1",
            name = "From Wallet",
            balance = BigDecimal("1000"),
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val oldToWallet = Wallet(
            id = "wallet2",
            name = "Old To Wallet",
            balance = BigDecimal("500"),
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newToWallet = Wallet(
            id = "wallet3",
            name = "New To Wallet",
            balance = BigDecimal("300"),
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val oldTransaction = Transaction(
            id = "tx1",
            amount = BigDecimal("100"),
            type = TransactionType.TRANSFER,
            description = "Transfer",
            categoryId = "cat1",
            fromWalletId = "wallet1",
            toWalletId = "wallet2",
            timestamp = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newTransaction = oldTransaction.copy(
            toWalletId = "wallet3"
        )

        coEvery { walletRepository.getWalletById("wallet1") } returns fromWallet
        coEvery { walletRepository.getWalletById("wallet2") } returns oldToWallet
        coEvery { walletRepository.getWalletById("wallet3") } returns newToWallet

        // When
        val result = updateTransactionUseCase(oldTransaction, newTransaction)

        // Then
        assertTrue(result.isSuccess)

        // Old destination wallet: 500 - 100 (reverse) = 400
        coVerify {
            walletRepository.updateWallet(
                match { it.id == "wallet2" && it.balance == BigDecimal("400") }
            )
        }

        // New destination wallet: 300 + 100 (apply) = 400
        coVerify {
            walletRepository.updateWallet(
                match { it.id == "wallet3" && it.balance == BigDecimal("400") }
            )
        }
    }
}

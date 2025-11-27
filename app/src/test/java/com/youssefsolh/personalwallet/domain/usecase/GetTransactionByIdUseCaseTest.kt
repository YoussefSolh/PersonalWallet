package com.youssefsolh.personalwallet.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for GetTransactionByIdUseCase
 * Tests transaction retrieval by ID functionality
 */
class GetTransactionByIdUseCaseTest {

    private lateinit var getTransactionByIdUseCase: GetTransactionByIdUseCase
    private val transactionRepository: TransactionRepository = mockk()

    private val testTransaction = Transaction(
        id = "tx1",
        fromWalletId = "wallet1",
        toWalletId = null,
        categoryId = "cat1",
        amount = BigDecimal("100"),
        description = "Test Transaction",
        timestamp = System.currentTimeMillis(),
        type = TransactionType.EXPENSE,
        isDeleted = false
    )

    @Before
    fun setup() {
        getTransactionByIdUseCase = GetTransactionByIdUseCase(transactionRepository)
    }

    @Test
    fun `invoke should return transaction when it exists`() = runTest {
        // Given
        coEvery { transactionRepository.getTransactionById("tx1") } returns testTransaction

        // When
        val result = getTransactionByIdUseCase("tx1")

        // Then
        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(testTransaction)
        assertThat(result?.id).isEqualTo("tx1")
        assertThat(result?.amount).isEqualTo(BigDecimal("100"))
        assertThat(result?.type).isEqualTo(TransactionType.EXPENSE)
    }

    @Test
    fun `invoke should return null when transaction does not exist`() = runTest {
        // Given
        coEvery { transactionRepository.getTransactionById("nonexistent") } returns null

        // When
        val result = getTransactionByIdUseCase("nonexistent")

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `invoke should return expense transaction`() = runTest {
        // Given
        val expenseTransaction = testTransaction.copy(
            id = "expense1",
            type = TransactionType.EXPENSE,
            amount = BigDecimal("50.25")
        )
        coEvery { transactionRepository.getTransactionById("expense1") } returns expenseTransaction

        // When
        val result = getTransactionByIdUseCase("expense1")

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.type).isEqualTo(TransactionType.EXPENSE)
        assertThat(result?.amount).isEqualTo(BigDecimal("50.25"))
    }

    @Test
    fun `invoke should return income transaction`() = runTest {
        // Given
        val incomeTransaction = testTransaction.copy(
            id = "income1",
            type = TransactionType.INCOME,
            amount = BigDecimal("1000"),
            description = "Salary"
        )
        coEvery { transactionRepository.getTransactionById("income1") } returns incomeTransaction

        // When
        val result = getTransactionByIdUseCase("income1")

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.type).isEqualTo(TransactionType.INCOME)
        assertThat(result?.description).isEqualTo("Salary")
        assertThat(result?.amount).isEqualTo(BigDecimal("1000"))
    }

    @Test
    fun `invoke should return transfer transaction`() = runTest {
        // Given
        val transferTransaction = testTransaction.copy(
            id = "transfer1",
            type = TransactionType.TRANSFER,
            fromWalletId = "wallet1",
            toWalletId = "wallet2",
            amount = BigDecimal("200"),
            description = "Transfer between wallets"
        )
        coEvery { transactionRepository.getTransactionById("transfer1") } returns transferTransaction

        // When
        val result = getTransactionByIdUseCase("transfer1")

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.type).isEqualTo(TransactionType.TRANSFER)
        assertThat(result?.fromWalletId).isEqualTo("wallet1")
        assertThat(result?.toWalletId).isEqualTo("wallet2")
        assertThat(result?.amount).isEqualTo(BigDecimal("200"))
    }

    @Test
    fun `invoke should handle transaction with large amount`() = runTest {
        // Given
        val largeTransaction = testTransaction.copy(
            id = "large1",
            amount = BigDecimal("99999.99")
        )
        coEvery { transactionRepository.getTransactionById("large1") } returns largeTransaction

        // When
        val result = getTransactionByIdUseCase("large1")

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.amount).isEqualTo(BigDecimal("99999.99"))
    }

    @Test
    fun `invoke should handle transaction with empty description`() = runTest {
        // Given
        val emptyDescTransaction = testTransaction.copy(
            id = "empty1",
            description = ""
        )
        coEvery { transactionRepository.getTransactionById("empty1") } returns emptyDescTransaction

        // When
        val result = getTransactionByIdUseCase("empty1")

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.description).isEmpty()
    }

    @Test
    fun `invoke should handle transaction with different category`() = runTest {
        // Given
        val transaction = testTransaction.copy(
            id = "cat2",
            categoryId = "category2"
        )
        coEvery { transactionRepository.getTransactionById("cat2") } returns transaction

        // When
        val result = getTransactionByIdUseCase("cat2")

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.categoryId).isEqualTo("category2")
    }

    @Test
    fun `invoke should handle deleted transaction`() = runTest {
        // Given
        val deletedTransaction = testTransaction.copy(
            id = "deleted1",
            isDeleted = true
        )
        coEvery { transactionRepository.getTransactionById("deleted1") } returns deletedTransaction

        // When
        val result = getTransactionByIdUseCase("deleted1")

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.isDeleted).isTrue()
    }

    @Test
    fun `invoke should be called multiple times with different ids`() = runTest {
        // Given
        val tx1 = testTransaction.copy(id = "tx1")
        val tx2 = testTransaction.copy(id = "tx2")
        val tx3 = testTransaction.copy(id = "tx3")

        coEvery { transactionRepository.getTransactionById("tx1") } returns tx1
        coEvery { transactionRepository.getTransactionById("tx2") } returns tx2
        coEvery { transactionRepository.getTransactionById("tx3") } returns tx3

        // When
        val result1 = getTransactionByIdUseCase("tx1")
        val result2 = getTransactionByIdUseCase("tx2")
        val result3 = getTransactionByIdUseCase("tx3")

        // Then
        assertThat(result1?.id).isEqualTo("tx1")
        assertThat(result2?.id).isEqualTo("tx2")
        assertThat(result3?.id).isEqualTo("tx3")
    }

    @Test
    fun `invoke should preserve timestamp`() = runTest {
        // Given
        val timestamp = 1234567890000L
        val transaction = testTransaction.copy(
            id = "time1",
            timestamp = timestamp
        )
        coEvery { transactionRepository.getTransactionById("time1") } returns transaction

        // When
        val result = getTransactionByIdUseCase("time1")

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.timestamp).isEqualTo(timestamp)
    }
}

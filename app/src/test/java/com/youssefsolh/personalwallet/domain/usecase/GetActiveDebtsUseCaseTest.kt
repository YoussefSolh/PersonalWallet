package com.youssefsolh.personalwallet.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class GetActiveDebtsUseCaseTest {

    private lateinit var getActiveDebtsUseCase: GetActiveDebtsUseCase
    private val transactionRepository: TransactionRepository = mockk()

    @Before
    fun setup() {
        getActiveDebtsUseCase = GetActiveDebtsUseCase(transactionRepository)
    }

    @Test
    fun `invoke should return debt transactions`() = runTest {
        // Given
        val debts = listOf(
            Transaction(id = "tx1", fromWalletId = "w1", toWalletId = null, categoryId = "cat1", amount = BigDecimal("100"), description = "Debt 1", timestamp = 1L, type = TransactionType.EXPENSE, isDebt = true),
            Transaction(id = "tx2", fromWalletId = "w1", toWalletId = null, categoryId = "cat1", amount = BigDecimal("200"), description = "Debt 2", timestamp = 1L, type = TransactionType.EXPENSE, isDebt = true)
        )
        coEvery { transactionRepository.getDebtTransactions() } returns flowOf(debts)

        // When
        getActiveDebtsUseCase().test {
            val result = awaitItem()

            // Then
            assertThat(result).hasSize(2)
            assertThat(result).containsExactlyElementsIn(debts)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should return empty list when no debts`() = runTest {
        // Given
        coEvery { transactionRepository.getDebtTransactions() } returns flowOf(emptyList())

        // When
        getActiveDebtsUseCase().test {
            val result = awaitItem()

            // Then
            assertThat(result).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }
}

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
            Transaction("tx1", "w1", null, "cat1", BigDecimal("100"), "Debt 1", 1L, TransactionType.EXPENSE, false),
            Transaction("tx2", "w1", null, "cat1", BigDecimal("200"), "Debt 2", 1L, TransactionType.EXPENSE, false)
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

package com.youssefsolh.personalwallet.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetCategoriesUseCase
 * Tests category retrieval by transaction type
 */
class GetCategoriesUseCaseTest {

    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private val categoryRepository: CategoryRepository = mockk()

    private val expenseCategories = listOf(
        Category(
            id = "cat1",
            name = "Food",
            icon = "🍔",
            color = 0xFF0000,
            type = TransactionType.EXPENSE
        ),
        Category(
            id = "cat2",
            name = "Transport",
            icon = "🚗",
            color = 0x00FF00,
            type = TransactionType.EXPENSE
        )
    )

    private val incomeCategories = listOf(
        Category(
            id = "cat3",
            name = "Salary",
            icon = "💰",
            color = 0x0000FF,
            type = TransactionType.INCOME
        ),
        Category(
            id = "cat4",
            name = "Bonus",
            icon = "🎁",
            color = 0xFFFF00,
            type = TransactionType.INCOME
        )
    )

    @Before
    fun setup() {
        getCategoriesUseCase = GetCategoriesUseCase(categoryRepository)
    }

    @Test
    fun `invoke with EXPENSE type should return expense categories`() = runTest {
        // Given
        coEvery {
            categoryRepository.getCategoriesByType(TransactionType.EXPENSE)
        } returns flowOf(expenseCategories)

        // When
        getCategoriesUseCase(TransactionType.EXPENSE).test {
            val result = awaitItem()

            // Then
            assertThat(result).hasSize(2)
            assertThat(result).containsExactlyElementsIn(expenseCategories)
            assertThat(result.all { it.type == TransactionType.EXPENSE }).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke with INCOME type should return income categories`() = runTest {
        // Given
        coEvery {
            categoryRepository.getCategoriesByType(TransactionType.INCOME)
        } returns flowOf(incomeCategories)

        // When
        getCategoriesUseCase(TransactionType.INCOME).test {
            val result = awaitItem()

            // Then
            assertThat(result).hasSize(2)
            assertThat(result).containsExactlyElementsIn(incomeCategories)
            assertThat(result.all { it.type == TransactionType.INCOME }).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke with TRANSFER type should return empty list`() = runTest {
        // Given - Transfer transactions don't have categories
        coEvery {
            categoryRepository.getCategoriesByType(TransactionType.TRANSFER)
        } returns flowOf(emptyList())

        // When
        getCategoriesUseCase(TransactionType.TRANSFER).test {
            val result = awaitItem()

            // Then
            assertThat(result).isEmpty()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should return empty list when no categories exist for type`() = runTest {
        // Given
        coEvery {
            categoryRepository.getCategoriesByType(TransactionType.EXPENSE)
        } returns flowOf(emptyList())

        // When
        getCategoriesUseCase(TransactionType.EXPENSE).test {
            val result = awaitItem()

            // Then
            assertThat(result).isEmpty()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should return single category`() = runTest {
        // Given
        val singleCategory = listOf(
            Category("cat1", "Food", "🍔", 0xFF0000, TransactionType.EXPENSE)
        )
        coEvery {
            categoryRepository.getCategoriesByType(TransactionType.EXPENSE)
        } returns flowOf(singleCategory)

        // When
        getCategoriesUseCase(TransactionType.EXPENSE).test {
            val result = awaitItem()

            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0].name).isEqualTo("Food")
            assertThat(result[0].icon).isEqualTo("🍔")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should preserve category order from repository`() = runTest {
        // Given - Categories in specific order
        val orderedCategories = listOf(
            Category("cat3", "Shopping", "🛍️", 0xAAAAAA, TransactionType.EXPENSE),
            Category("cat1", "Food", "🍔", 0xFF0000, TransactionType.EXPENSE),
            Category("cat2", "Transport", "🚗", 0x00FF00, TransactionType.EXPENSE)
        )
        coEvery {
            categoryRepository.getCategoriesByType(TransactionType.EXPENSE)
        } returns flowOf(orderedCategories)

        // When
        getCategoriesUseCase(TransactionType.EXPENSE).test {
            val result = awaitItem()

            // Then - Order should be preserved
            assertThat(result[0].name).isEqualTo("Shopping")
            assertThat(result[1].name).isEqualTo("Food")
            assertThat(result[2].name).isEqualTo("Transport")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should handle repository emitting multiple updates`() = runTest {
        // Given - Repository emits updates
        val initial = listOf(
            Category("cat1", "Food", "🍔", 0xFF0000, TransactionType.EXPENSE)
        )
        val updated = listOf(
            Category("cat1", "Food", "🍔", 0xFF0000, TransactionType.EXPENSE),
            Category("cat2", "Transport", "🚗", 0x00FF00, TransactionType.EXPENSE)
        )

        coEvery {
            categoryRepository.getCategoriesByType(TransactionType.EXPENSE)
        } returns flowOf(initial, updated)

        // When
        getCategoriesUseCase(TransactionType.EXPENSE).test {
            // First emission
            val first = awaitItem()
            assertThat(first).hasSize(1)

            // Second emission
            val second = awaitItem()
            assertThat(second).hasSize(2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should handle categories with different colors`() = runTest {
        // Given
        val colorfulCategories = listOf(
            Category("cat1", "Red", "🔴", 0xFF0000, TransactionType.EXPENSE),
            Category("cat2", "Green", "🟢", 0x00FF00, TransactionType.EXPENSE),
            Category("cat3", "Blue", "🔵", 0x0000FF, TransactionType.EXPENSE)
        )
        coEvery {
            categoryRepository.getCategoriesByType(TransactionType.EXPENSE)
        } returns flowOf(colorfulCategories)

        // When
        getCategoriesUseCase(TransactionType.EXPENSE).test {
            val result = awaitItem()

            // Then
            assertThat(result).hasSize(3)
            assertThat(result[0].color).isEqualTo(0xFF0000)
            assertThat(result[1].color).isEqualTo(0x00FF00)
            assertThat(result[2].color).isEqualTo(0x0000FF)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke should handle categories with different icons`() = runTest {
        // Given
        val categories = listOf(
            Category("cat1", "Food", "🍔", 0xFF0000, TransactionType.EXPENSE),
            Category("cat2", "Shopping", "🛍️", 0x00FF00, TransactionType.EXPENSE),
            Category("cat3", "Bills", "💵", 0x0000FF, TransactionType.EXPENSE)
        )
        coEvery {
            categoryRepository.getCategoriesByType(TransactionType.EXPENSE)
        } returns flowOf(categories)

        // When
        getCategoriesUseCase(TransactionType.EXPENSE).test {
            val result = awaitItem()

            // Then
            assertThat(result).hasSize(3)
            assertThat(result[0].icon).isEqualTo("🍔")
            assertThat(result[1].icon).isEqualTo("🛍️")
            assertThat(result[2].icon).isEqualTo("💵")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke called multiple times should query repository each time`() = runTest {
        // Given
        coEvery {
            categoryRepository.getCategoriesByType(any())
        } returns flowOf(expenseCategories)

        // When
        getCategoriesUseCase(TransactionType.EXPENSE).test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        getCategoriesUseCase(TransactionType.INCOME).test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        // Then - Should have called repository for both types
        io.mockk.coVerify {
            categoryRepository.getCategoriesByType(TransactionType.EXPENSE)
            categoryRepository.getCategoriesByType(TransactionType.INCOME)
        }
    }
}

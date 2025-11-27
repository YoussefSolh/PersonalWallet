package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateCategoryUseCaseTest {

    private lateinit var createCategoryUseCase: CreateCategoryUseCase
    private val categoryRepository: CategoryRepository = mockk()

    @Before
    fun setup() {
        createCategoryUseCase = CreateCategoryUseCase(categoryRepository)
    }

    @Test
    fun `invoke should insert expense category`() = runTest {
        // Given
        val category = Category("cat1", "Food", "🍔", 0xFF0000, TransactionType.EXPENSE)
        coEvery { categoryRepository.insertCategory(category) } returns Unit

        // When
        createCategoryUseCase(category)

        // Then
        coVerify { categoryRepository.insertCategory(category) }
    }

    @Test
    fun `invoke should insert income category`() = runTest {
        // Given
        val category = Category("cat1", "Salary", "💰", 0x00FF00, TransactionType.INCOME)
        coEvery { categoryRepository.insertCategory(category) } returns Unit

        // When
        createCategoryUseCase(category)

        // Then
        coVerify { categoryRepository.insertCategory(category) }
    }

    @Test
    fun `invoke should handle different colors`() = runTest {
        // Given
        val category = Category("cat1", "Test", "✅", 0xABCDEF, TransactionType.EXPENSE)
        coEvery { categoryRepository.insertCategory(any()) } returns Unit

        // When
        createCategoryUseCase(category)

        // Then
        coVerify { categoryRepository.insertCategory(match { it.color == 0xABCDEF }) }
    }
}

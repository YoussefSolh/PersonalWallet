package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Use case for deleting a category
 */
class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: String): Result<Unit> {
        return try {
            categoryRepository.deleteCategory(categoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

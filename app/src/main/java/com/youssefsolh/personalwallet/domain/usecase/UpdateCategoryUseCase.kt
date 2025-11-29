package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Use case for updating an existing category
 */
class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category) {
        categoryRepository.updateCategory(category)
    }
}

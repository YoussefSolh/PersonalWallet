package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Use case for retrieving a category by its ID
 */
class GetCategoryByIdUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(id: String): Category? {
        return categoryRepository.getCategoryById(id)
    }
}

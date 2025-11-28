package com.youssefsolh.personalwallet.data.repository

import com.youssefsolh.personalwallet.data.local.CurrentUserProvider
import com.youssefsolh.personalwallet.data.local.dao.CategoryDao
import com.youssefsolh.personalwallet.data.local.entity.toDomain
import com.youssefsolh.personalwallet.data.local.entity.toEntity
import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val currentUserProvider: CurrentUserProvider
) : CategoryRepository {

    override suspend fun getAllCategories(): Flow<List<Category>> {
        val userId = currentUserProvider.getCurrentUserId()
        return categoryDao.getAllCategories(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoriesByType(type: TransactionType): Flow<List<Category>> {
        val userId = currentUserProvider.getCurrentUserId()
        return categoryDao.getCategoriesByType(userId, type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(id: String): Category? {
        val userId = currentUserProvider.getCurrentUserId()
        return categoryDao.getCategoryById(userId, id)?.toDomain()
    }

    override suspend fun insertCategory(category: Category) {
        val userId = currentUserProvider.getCurrentUserId()
        categoryDao.insertCategory(category.toEntity(userId))
    }

    override suspend fun updateCategory(category: Category) {
        val userId = currentUserProvider.getCurrentUserId()
        categoryDao.updateCategory(category.toEntity(userId))
    }

    override suspend fun deleteCategory(id: String) {
        categoryDao.deleteCategory(id)
    }

    override suspend fun getDefaultCategories(): List<Category> {
        val userId = currentUserProvider.getCurrentUserId()
        return categoryDao.getDefaultCategories(userId).map { it.toDomain() }
    }
}
package com.youssefsolh.personalwallet.data.local.dao

import androidx.room.*
import com.youssefsolh.personalwallet.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE (userId = :userId OR userId = 'default') AND isDeleted = 0 ORDER BY isDefault DESC, name ASC")
    fun getAllCategories(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE (userId = :userId OR userId = 'default') AND type = :type AND isDeleted = 0 ORDER BY isDefault DESC, name ASC")
    fun getCategoriesByType(userId: String, type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE (userId = :userId OR userId = 'default') AND id = :id AND isDeleted = 0")
    suspend fun getCategoryById(userId: String, id: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE (userId = :userId OR userId = 'default') AND isDefault = 1 AND isDeleted = 0")
    suspend fun getDefaultCategories(userId: String): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("UPDATE categories SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun deleteCategory(id: String, timestamp: Long = System.currentTimeMillis())

    // Never matches the shared 'default' categories
    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteAllCategoriesForUser(userId: String)
}
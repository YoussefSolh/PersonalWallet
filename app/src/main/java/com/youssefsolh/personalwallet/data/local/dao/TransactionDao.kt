package com.youssefsolh.personalwallet.data.local.dao

import androidx.room.*
import com.youssefsolh.personalwallet.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId AND isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND (fromWalletId = :walletId OR toWalletId = :walletId) AND isDeleted = 0 ORDER BY timestamp DESC")
    fun getTransactionsByWallet(userId: String, walletId: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId
        AND (fromWalletId = :walletId OR toWalletId = :walletId)
        AND isDeleted = 0
        AND (:type IS NULL OR type = :type)
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:startDate IS NULL OR timestamp >= :startDate)
        AND (:endDate IS NULL OR timestamp <= :endDate)
        AND (:searchQuery IS NULL OR description LIKE '%' || :searchQuery || '%')
        ORDER BY timestamp DESC
    """)
    fun getTransactionsByWalletFiltered(
        userId: String,
        walletId: String,
        type: String? = null,
        categoryId: String? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        searchQuery: String? = null
    ): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND id = :id AND isDeleted = 0")
    suspend fun getTransactionById(userId: String, id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE userId = :userId AND isDebt = 1 AND debtSettled = 0 AND isDeleted = 0 ORDER BY timestamp DESC")
    fun getDebtTransactions(userId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun deleteTransaction(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE transactions SET debtSettled = 1, relatedTransactionId = :settlementId, updatedAt = :timestamp WHERE id = :debtId")
    suspend fun settleDebt(debtId: String, settlementId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("""
        SELECT categoryId, SUM(CAST(amount AS REAL)) as total, COUNT(*) as count
        FROM transactions
        WHERE userId = :userId
        AND type = 'EXPENSE'
        AND isDeleted = 0
        AND timestamp BETWEEN :startDate AND :endDate
        GROUP BY categoryId
        ORDER BY total DESC
    """)
    suspend fun getSpendingByCategory(userId: String, startDate: Long, endDate: Long): List<CategorySpendingEntity>

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN CAST(amount AS REAL) ELSE 0 END), 0) as totalIncome,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN CAST(amount AS REAL) ELSE 0 END), 0) as totalExpense
        FROM transactions
        WHERE userId = :userId
        AND isDeleted = 0
        AND timestamp BETWEEN :startDate AND :endDate
    """)
    suspend fun getIncomeExpenseSummary(userId: String, startDate: Long, endDate: Long): IncomeExpenseSummaryEntity
}

data class CategorySpendingEntity(
    val categoryId: String,
    val total: Double,
    val count: Int
)

data class IncomeExpenseSummaryEntity(
    val totalIncome: Double,
    val totalExpense: Double
)
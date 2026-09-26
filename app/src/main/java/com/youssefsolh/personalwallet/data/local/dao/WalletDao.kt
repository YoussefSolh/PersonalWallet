package com.youssefsolh.personalwallet.data.local.dao

import androidx.room.*
import com.youssefsolh.personalwallet.data.local.entity.WalletEntity
import com.youssefsolh.personalwallet.data.local.entity.WalletWithCurrencySymbol
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("""
        SELECT w.*, c.symbol as currencySymbol
        FROM wallets w
        LEFT JOIN currencies c ON w.currency = c.code
        WHERE w.userId = :userId AND w.isDeleted = 0
        ORDER BY w.createdAt ASC
    """)
    fun getAllWallets(userId: String): Flow<List<WalletWithCurrencySymbol>>

    @Query("""
        SELECT w.*, c.symbol as currencySymbol
        FROM wallets w
        LEFT JOIN currencies c ON w.currency = c.code
        WHERE w.userId = :userId AND w.isDeleted = 0
        ORDER BY w.createdAt ASC
    """)
    suspend fun getAllWalletsSync(userId: String): List<WalletWithCurrencySymbol>

    @Query("""
        SELECT w.*, c.symbol as currencySymbol
        FROM wallets w
        LEFT JOIN currencies c ON w.currency = c.code
        WHERE w.id = :id AND w.userId = :userId AND w.isDeleted = 0
    """)
    suspend fun getWalletById(id: String, userId: String): WalletWithCurrencySymbol?

    @Query("SELECT balance FROM wallets WHERE id = :id AND userId = :userId AND isDeleted = 0")
    fun getWalletBalance(id: String, userId: String): Flow<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Query("UPDATE wallets SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun deleteWallet(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM wallets WHERE userId = :userId")
    suspend fun deleteAllWalletsForUser(userId: String)
}
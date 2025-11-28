package com.youssefsolh.personalwallet.data.local.dao

import androidx.room.*
import com.youssefsolh.personalwallet.data.local.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt ASC")
    fun getAllWallets(userId: String): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt ASC")
    suspend fun getAllWalletsSync(userId: String): List<WalletEntity>

    @Query("SELECT * FROM wallets WHERE id = :id AND userId = :userId AND isDeleted = 0")
    suspend fun getWalletById(id: String, userId: String): WalletEntity?

    @Query("SELECT balance FROM wallets WHERE id = :id AND userId = :userId AND isDeleted = 0")
    fun getWalletBalance(id: String, userId: String): Flow<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Query("UPDATE wallets SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun deleteWallet(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM wallets")
    suspend fun deleteAllWallets()
}
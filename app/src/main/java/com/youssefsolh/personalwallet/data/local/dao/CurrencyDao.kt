package com.youssefsolh.personalwallet.data.local.dao

import androidx.room.*
import com.youssefsolh.personalwallet.data.local.entity.CurrencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currencies ORDER BY is_default DESC, code ASC")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>

    @Query("SELECT * FROM currencies WHERE is_default = 1 LIMIT 1")
    fun getDefaultCurrency(): Flow<CurrencyEntity?>

    @Query("SELECT * FROM currencies WHERE is_default = 1 LIMIT 1")
    suspend fun getDefaultCurrencySync(): CurrencyEntity?

    @Query("SELECT * FROM currencies WHERE code = :code")
    suspend fun getCurrencyByCode(code: String): CurrencyEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCurrency(currency: CurrencyEntity)

    @Update
    suspend fun updateCurrency(currency: CurrencyEntity)

    @Query("DELETE FROM currencies WHERE code = :code AND is_system_currency = 0 AND is_default = 0")
    suspend fun deleteCurrency(code: String): Int

    @Query("SELECT COUNT(*) FROM wallets WHERE currency = :code AND isDeleted = 0")
    suspend fun countWalletsUsingCurrency(code: String): Int
}

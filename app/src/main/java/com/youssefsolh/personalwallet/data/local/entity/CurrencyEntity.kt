package com.youssefsolh.personalwallet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.youssefsolh.personalwallet.domain.model.Currency
import java.math.BigDecimal

/**
 * Column names mirror the `currencies` table created in MIGRATION_5_6.
 */
@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey
    val code: String,
    val name: String,
    val symbol: String,
    @ColumnInfo(name = "exchange_rate_to_default")
    val exchangeRateToDefault: String,
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,
    @ColumnInfo(name = "is_system_currency")
    val isSystemCurrency: Boolean = false,
    @ColumnInfo(name = "is_manual_rate")
    val isManualRate: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_rate_update")
    val lastRateUpdate: Long? = null
)

fun CurrencyEntity.toDomain(): Currency {
    return Currency(
        code = code,
        name = name,
        symbol = symbol,
        exchangeRateToDefault = BigDecimal(exchangeRateToDefault),
        isDefault = isDefault,
        isSystemCurrency = isSystemCurrency,
        isManualRate = isManualRate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastRateUpdate = lastRateUpdate
    )
}

fun Currency.toEntity(): CurrencyEntity {
    return CurrencyEntity(
        code = code,
        name = name,
        symbol = symbol,
        exchangeRateToDefault = exchangeRateToDefault.toPlainString(),
        isDefault = isDefault,
        isSystemCurrency = isSystemCurrency,
        isManualRate = isManualRate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastRateUpdate = lastRateUpdate
    )
}

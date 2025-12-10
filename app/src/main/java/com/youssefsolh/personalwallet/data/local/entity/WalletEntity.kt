package com.youssefsolh.personalwallet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.youssefsolh.personalwallet.domain.model.Wallet
import java.math.BigDecimal

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey
    val id: String,
    val userId: String,  // User ID for data isolation
    val name: String,
    val balance: String,
    val currency: String = "USD",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

/**
 * Data class for wallet joined with currency symbol from currencies table.
 */
data class WalletWithCurrencySymbol(
    val id: String,
    val userId: String,
    val name: String,
    val balance: String,
    val currency: String,
    @ColumnInfo(name = "currencySymbol")
    val currencySymbol: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean
)

fun WalletEntity.toDomain(): Wallet {
    return Wallet(
        id = id,
        name = name,
        balance = BigDecimal(balance),
        currency = currency,
        currencySymbol = "$",  // Default for backward compatibility
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )
}

fun WalletWithCurrencySymbol.toDomain(): Wallet {
    return Wallet(
        id = id,
        name = name,
        balance = BigDecimal(balance),
        currency = currency,
        currencySymbol = currencySymbol ?: "$",  // Fallback to $ if null
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )
}

fun Wallet.toEntity(userId: String): WalletEntity {
    return WalletEntity(
        id = id,
        userId = userId,
        name = name,
        balance = balance.toString(),
        currency = currency,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )
}
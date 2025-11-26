package com.youssefsolh.personalwallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.youssefsolh.personalwallet.domain.model.Wallet
import java.math.BigDecimal

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val balance: String,
    val currency: String = "USD",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

fun WalletEntity.toDomain(): Wallet {
    return Wallet(
        id = id,
        name = name,
        balance = BigDecimal(balance),
        currency = currency,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )
}

fun Wallet.toEntity(): WalletEntity {
    return WalletEntity(
        id = id,
        name = name,
        balance = balance.toString(),
        currency = currency,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )
}
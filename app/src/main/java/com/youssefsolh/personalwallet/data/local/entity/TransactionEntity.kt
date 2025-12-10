package com.youssefsolh.personalwallet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import java.math.BigDecimal

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,  // User ID for data isolation
    val amount: String,
    val type: String,
    val description: String,
    val categoryId: String,
    val fromWalletId: String?,
    val toWalletId: String?,
    val isDebt: Boolean = false,
    val debtSettled: Boolean = false,
    val relatedTransactionId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,

    // Multi-currency fields
    @ColumnInfo(name = "original_currency")
    val originalCurrency: String = "USD",  // ISO code (USD, EUR, etc.) - currency of the wallet

    @ColumnInfo(name = "default_currency")
    val defaultCurrency: String = "USD",  // ISO code of default currency at time of transaction

    @ColumnInfo(name = "amount_in_default_currency")
    val amountInDefaultCurrency: String = "0.0",  // Converted amount (stored as String for precision)

    @ColumnInfo(name = "exchange_rate")
    val exchangeRate: String = "1.0"  // Rate used for conversion (immutable, stored as String for precision)
)

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = BigDecimal(amount),
        type = TransactionType.valueOf(type),
        description = description,
        categoryId = categoryId,
        fromWalletId = fromWalletId,
        toWalletId = toWalletId,
        isDebt = isDebt,
        debtSettled = debtSettled,
        relatedTransactionId = relatedTransactionId,
        timestamp = timestamp,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        originalCurrency = originalCurrency,
        defaultCurrency = defaultCurrency,
        amountInDefaultCurrency = BigDecimal(amountInDefaultCurrency),
        exchangeRate = BigDecimal(exchangeRate)
    )
}

fun Transaction.toEntity(userId: String): TransactionEntity {
    return TransactionEntity(
        id = id,
        userId = userId,
        amount = amount.toString(),
        type = type.name,
        description = description,
        categoryId = categoryId,
        fromWalletId = fromWalletId,
        toWalletId = toWalletId,
        isDebt = isDebt,
        debtSettled = debtSettled,
        relatedTransactionId = relatedTransactionId,
        timestamp = timestamp,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        originalCurrency = originalCurrency,
        defaultCurrency = defaultCurrency,
        amountInDefaultCurrency = amountInDefaultCurrency.toString(),
        exchangeRate = exchangeRate.toString()
    )
}
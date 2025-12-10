package com.youssefsolh.personalwallet.domain.model

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Transaction(
    val id: String,
    val amount: @Serializable(with = BigDecimalSerializer::class) BigDecimal,
    val type: TransactionType,
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
    val originalCurrency: String = "USD", // ISO code of the wallet's currency
    val defaultCurrency: String = "USD", // ISO code of default currency at time of transaction
    val amountInDefaultCurrency: @Serializable(with = BigDecimalSerializer::class) BigDecimal = BigDecimal.ZERO, // Converted amount
    val exchangeRate: @Serializable(with = BigDecimalSerializer::class) BigDecimal = BigDecimal.ONE // Immutable rate at time of transaction
)
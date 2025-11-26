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
    val isDeleted: Boolean = false
)
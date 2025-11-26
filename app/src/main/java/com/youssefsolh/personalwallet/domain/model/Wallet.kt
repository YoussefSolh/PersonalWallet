package com.youssefsolh.personalwallet.domain.model

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Wallet(
    val id: String,
    val name: String,
    val balance: @Serializable(with = BigDecimalSerializer::class) BigDecimal,
    val currency: String = "USD",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
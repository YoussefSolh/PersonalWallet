package com.youssefsolh.personalwallet.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val wallets: List<Wallet>,
    val transactions: List<Transaction>,
    val categories: List<Category>,
    val timestamp: Long = System.currentTimeMillis(),
    val version: String = "1.0"
)

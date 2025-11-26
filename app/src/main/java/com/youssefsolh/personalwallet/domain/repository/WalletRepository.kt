package com.youssefsolh.personalwallet.domain.repository

import com.youssefsolh.personalwallet.domain.model.Wallet
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface WalletRepository {
    suspend fun getAllWallets(): Flow<List<Wallet>>
    suspend fun getWalletById(id: String): Wallet?
    suspend fun insertWallet(wallet: Wallet)
    suspend fun updateWallet(wallet: Wallet)
    suspend fun deleteWallet(id: String)
    suspend fun getWalletBalance(id: String): Flow<BigDecimal>
}
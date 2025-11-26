package com.youssefsolh.personalwallet.data.repository

import com.youssefsolh.personalwallet.data.local.dao.WalletDao
import com.youssefsolh.personalwallet.data.local.entity.toDomain
import com.youssefsolh.personalwallet.data.local.entity.toEntity
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao
) : WalletRepository {

    override suspend fun getAllWallets(): Flow<List<Wallet>> {
        return walletDao.getAllWallets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getWalletById(id: String): Wallet? {
        return walletDao.getWalletById(id)?.toDomain()
    }

    override suspend fun insertWallet(wallet: Wallet) {
        walletDao.insertWallet(wallet.toEntity())
    }

    override suspend fun updateWallet(wallet: Wallet) {
        walletDao.updateWallet(wallet.toEntity())
    }

    override suspend fun deleteWallet(id: String) {
        walletDao.deleteWallet(id)
    }

    override suspend fun getWalletBalance(id: String): Flow<BigDecimal> {
        return walletDao.getWalletBalance(id).map { balanceString ->
            BigDecimal(balanceString)
        }
    }
}
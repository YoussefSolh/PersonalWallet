package com.youssefsolh.personalwallet.data.repository

import com.youssefsolh.personalwallet.data.local.CurrentUserProvider
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
    private val walletDao: WalletDao,
    private val currentUserProvider: CurrentUserProvider
) : WalletRepository {

    override suspend fun getAllWallets(): Flow<List<Wallet>> {
        val userId = currentUserProvider.getCurrentUserId()
        return walletDao.getAllWallets(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getWalletById(id: String): Wallet? {
        val userId = currentUserProvider.getCurrentUserId()
        return walletDao.getWalletById(id, userId)?.toDomain()
    }

    override suspend fun insertWallet(wallet: Wallet) {
        val userId = currentUserProvider.getCurrentUserId()
        walletDao.insertWallet(wallet.toEntity(userId))
    }

    override suspend fun updateWallet(wallet: Wallet) {
        val userId = currentUserProvider.getCurrentUserId()
        walletDao.updateWallet(wallet.toEntity(userId))
    }

    override suspend fun deleteWallet(id: String) {
        walletDao.deleteWallet(id)
    }

    override suspend fun getWalletBalance(id: String): Flow<BigDecimal> {
        val userId = currentUserProvider.getCurrentUserId()
        return walletDao.getWalletBalance(id, userId).map { balanceString ->
            BigDecimal(balanceString)
        }
    }
}
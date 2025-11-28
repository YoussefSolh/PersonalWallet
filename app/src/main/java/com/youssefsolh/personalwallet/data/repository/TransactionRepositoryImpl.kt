package com.youssefsolh.personalwallet.data.repository

import com.youssefsolh.personalwallet.data.local.CurrentUserProvider
import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.data.local.entity.toDomain
import com.youssefsolh.personalwallet.data.local.entity.toEntity
import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val currentUserProvider: CurrentUserProvider
) : TransactionRepository {

    override suspend fun getAllTransactions(): Flow<List<Transaction>> {
        val userId = currentUserProvider.getCurrentUserId()
        return transactionDao.getAllTransactions(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTransactionsByWallet(walletId: String): Flow<List<Transaction>> {
        val userId = currentUserProvider.getCurrentUserId()
        return transactionDao.getTransactionsByWallet(userId, walletId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTransactionsByWalletFiltered(
        walletId: String,
        type: TransactionType?,
        categoryId: String?,
        startDate: Long?,
        endDate: Long?,
        searchQuery: String?
    ): Flow<List<Transaction>> {
        val userId = currentUserProvider.getCurrentUserId()
        return transactionDao.getTransactionsByWalletFiltered(
            userId = userId,
            walletId = walletId,
            type = type?.name,
            categoryId = categoryId,
            startDate = startDate,
            endDate = endDate,
            searchQuery = searchQuery
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        val userId = currentUserProvider.getCurrentUserId()
        return transactionDao.getTransactionById(userId, id)?.toDomain()
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        val userId = currentUserProvider.getCurrentUserId()
        transactionDao.insertTransaction(transaction.toEntity(userId))
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val userId = currentUserProvider.getCurrentUserId()
        transactionDao.updateTransaction(transaction.toEntity(userId))
    }

    override suspend fun deleteTransaction(id: String) {
        transactionDao.deleteTransaction(id)
    }

    override suspend fun getDebtTransactions(): Flow<List<Transaction>> {
        val userId = currentUserProvider.getCurrentUserId()
        return transactionDao.getDebtTransactions(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun settleDebt(debtTransactionId: String, settlementTransactionId: String) {
        transactionDao.settleDebt(debtTransactionId, settlementTransactionId)
    }
}
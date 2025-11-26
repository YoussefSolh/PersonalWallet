package com.youssefsolh.personalwallet.data.repository

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
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override suspend fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTransactionsByWallet(walletId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByWallet(walletId).map { entities ->
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
        return transactionDao.getTransactionsByWalletFiltered(
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
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(id: String) {
        transactionDao.deleteTransaction(id)
    }

    override suspend fun getDebtTransactions(): Flow<List<Transaction>> {
        return transactionDao.getDebtTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun settleDebt(debtTransactionId: String, settlementTransactionId: String) {
        transactionDao.settleDebt(debtTransactionId, settlementTransactionId)
    }
}
package com.youssefsolh.personalwallet.domain.repository

import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun getTransactionsByWallet(walletId: String): Flow<List<Transaction>>
    suspend fun getTransactionsByWalletFiltered(
        walletId: String,
        type: TransactionType? = null,
        categoryId: String? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        searchQuery: String? = null
    ): Flow<List<Transaction>>
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
    suspend fun getDebtTransactions(): Flow<List<Transaction>>
    suspend fun settleDebt(debtTransactionId: String, settlementTransactionId: String)
}
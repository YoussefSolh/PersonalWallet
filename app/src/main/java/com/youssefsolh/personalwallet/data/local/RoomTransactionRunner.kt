package com.youssefsolh.personalwallet.data.local

import androidx.room.withTransaction
import com.youssefsolh.personalwallet.domain.repository.TransactionRunner
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomTransactionRunner @Inject constructor(
    private val database: WalletDatabase
) : TransactionRunner {
    override suspend fun <R> runInTransaction(block: suspend () -> R): R {
        return database.withTransaction { block() }
    }
}

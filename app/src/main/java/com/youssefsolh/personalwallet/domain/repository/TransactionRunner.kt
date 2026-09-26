package com.youssefsolh.personalwallet.domain.repository

/**
 * Runs a block of repository calls atomically: either every write in [block]
 * is committed, or none are.
 */
interface TransactionRunner {
    suspend fun <R> runInTransaction(block: suspend () -> R): R
}

package com.youssefsolh.personalwallet.di

import android.content.Context
import android.util.Log
import com.youssefsolh.personalwallet.data.local.DatabaseKeyStore
import com.youssefsolh.personalwallet.data.local.DatabaseRecovery
import com.youssefsolh.personalwallet.data.local.WalletDatabase
import com.youssefsolh.personalwallet.data.local.dao.CategoryDao
import com.youssefsolh.personalwallet.data.local.dao.CurrencyDao
import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.data.local.dao.WalletDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val TAG = "DatabaseModule"

    @Provides
    @Singleton
    fun provideWalletDatabase(@ApplicationContext context: Context): WalletDatabase {
        // Load SQLCipher native library
        System.loadLibrary("sqlcipher")

        val database = openDatabase(context)
        try {
            // Open eagerly so a key mismatch is detected here, through Room, which
            // also runs any pending migrations.
            database.openHelper.writableDatabase
            return database
        } catch (e: Exception) {
            database.close()
            if (!DatabaseRecovery.isUndecryptable(e)) throw e

            // The stored key no longer matches the file. Keep the file (moved aside)
            // and start with an empty database; the user is told on next screen.
            Log.e(TAG, "Database cannot be decrypted with the current key", e)
            DatabaseRecovery.quarantine(context, WalletDatabase.DATABASE_NAME)
            return openDatabase(context).also { it.openHelper.writableDatabase }
        }
    }

    private fun openDatabase(context: Context): WalletDatabase {
        // SQLCipher 4.x defaults: page_size=4096, kdf_iter=256000, HMAC_SHA512, PBKDF2_HMAC_SHA512
        val factory = SupportOpenHelperFactory(DatabaseKeyStore.getOrCreatePassphrase(context))
        return WalletDatabase.buildDatabase(context, factory)
    }

    @Provides
    fun provideWalletDao(database: WalletDatabase): WalletDao = database.walletDao()

    @Provides
    fun provideTransactionDao(database: WalletDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideCategoryDao(database: WalletDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideCurrencyDao(database: WalletDatabase): CurrencyDao = database.currencyDao()
}
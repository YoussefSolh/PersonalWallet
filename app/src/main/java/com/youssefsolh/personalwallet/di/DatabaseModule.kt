package com.youssefsolh.personalwallet.di

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.youssefsolh.personalwallet.data.local.WalletDatabase
import com.youssefsolh.personalwallet.data.local.dao.CategoryDao
import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.data.local.dao.WalletDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWalletDatabase(@ApplicationContext context: Context): WalletDatabase {
        // Get or create database passphrase
        val passphrase = getDatabasePassphrase(context)
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context.applicationContext,
            WalletDatabase::class.java,
            WalletDatabase.DATABASE_NAME
        )
        .openHelperFactory(factory)
        .build()
    }

    /**
     * Gets the database passphrase from encrypted storage, or generates a new one if not exists
     */
    private fun getDatabasePassphrase(context: Context): ByteArray {
        try {
            // Create master key for encryption
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Use EncryptedSharedPreferences to store passphrase
            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "encrypted_db_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            // Get existing passphrase or generate new one
            val existingPassphrase = encryptedPrefs.getString("db_passphrase", null)

            return if (existingPassphrase != null) {
                // Use existing passphrase
                android.util.Base64.decode(existingPassphrase, android.util.Base64.DEFAULT)
            } else {
                // Generate new secure passphrase
                val newPassphrase = ByteArray(32).apply {
                    SecureRandom().nextBytes(this)
                }

                // Save passphrase for future use
                val encoded = android.util.Base64.encodeToString(newPassphrase, android.util.Base64.DEFAULT)
                encryptedPrefs.edit().putString("db_passphrase", encoded).apply()

                newPassphrase
            }
        } catch (e: Exception) {
            // Fallback to a hardcoded passphrase in case of error (not recommended for production)
            // This ensures app doesn't crash but should be logged
            android.util.Log.e("DatabaseModule", "Error getting passphrase", e)
            return "PersonalWalletSecureKey2025".toByteArray()
        }
    }

    @Provides
    fun provideWalletDao(database: WalletDatabase): WalletDao = database.walletDao()

    @Provides
    fun provideTransactionDao(database: WalletDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideCategoryDao(database: WalletDatabase): CategoryDao = database.categoryDao()
}
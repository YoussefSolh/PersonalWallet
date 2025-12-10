package com.youssefsolh.personalwallet.di

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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
import java.security.SecureRandom
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWalletDatabase(@ApplicationContext context: Context): WalletDatabase {
        // Load SQLCipher native library
        System.loadLibrary("sqlcipher")

        // Check if database file exists and handle encryption migration
        val dbFile = context.getDatabasePath(WalletDatabase.DATABASE_NAME)

        // If database file exists, verify it can be opened with current passphrase
        if (dbFile.exists()) {
            android.util.Log.d("DatabaseModule", "Database file exists at: ${dbFile.absolutePath}, size: ${dbFile.length()} bytes")
            try {
                // Try to verify the database is accessible with current passphrase
                val testPassphrase = getDatabasePassphrase(context)
                android.util.Log.d("DatabaseModule", "Retrieved passphrase for verification, length: ${testPassphrase.size} bytes")

                // Create a test factory with SQLCipher defaults
                // SQLCipher 4.x defaults are: page_size=4096, kdf_iter=256000, HMAC_SHA512, PBKDF2_HMAC_SHA512
                val testFactory = SupportOpenHelperFactory(testPassphrase)
                android.util.Log.d("DatabaseModule", "Attempting to verify database with current passphrase...")

                // Try to open and query the database using SupportOpenHelper
                val testHelper = testFactory.create(
                    androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                        .name(WalletDatabase.DATABASE_NAME)
                        .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(6) { // Current database version
                            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                                // Don't create anything - this should not be called for existing DB
                                android.util.Log.w("DatabaseModule", "onCreate called during verification - unexpected!")
                            }
                            override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                                // Don't upgrade - this should not be called during read-only check
                                android.util.Log.w("DatabaseModule", "onUpgrade called during verification - unexpected!")
                            }
                        })
                        .build()
                )

                // Try to open and query the database
                val db = testHelper.readableDatabase
                db.query("SELECT 1").use { cursor ->
                    if (cursor.moveToFirst()) {
                        android.util.Log.d("DatabaseModule", "Test query executed successfully")
                    }
                }
                testHelper.close()
                android.util.Log.i("DatabaseModule", "Database verified successfully with current passphrase")
            } catch (e: Exception) {
                android.util.Log.e("DatabaseModule", "Database cannot be opened with current passphrase: ${e.javaClass.simpleName} - ${e.message}", e)
                android.util.Log.e("DatabaseModule", "Stack trace: ", e)

                // Check if it's an HMAC/encryption error
                val isEncryptionError = e.message?.contains("hmac", ignoreCase = true) == true ||
                                       e.message?.contains("not a database", ignoreCase = true) == true ||
                                       e.message?.contains("file is encrypted", ignoreCase = true) == true

                if (isEncryptionError) {
                    android.util.Log.w("DatabaseModule", "Detected encryption/passphrase mismatch error - deleting database")

                    // Force delete WAL and SHM files BEFORE deleting the main database
                    // This ensures they don't interfere with the new database creation
                    val walFile = context.getDatabasePath("${WalletDatabase.DATABASE_NAME}-wal")
                    val shmFile = context.getDatabasePath("${WalletDatabase.DATABASE_NAME}-shm")
                    val journalFile = context.getDatabasePath("${WalletDatabase.DATABASE_NAME}-journal")

                    android.util.Log.d("DatabaseModule", "WAL file exists: ${walFile.exists()}, SHM file exists: ${shmFile.exists()}, Journal file exists: ${journalFile.exists()}")

                    // Try to delete these files multiple times if needed
                    if (walFile.exists()) {
                        val walDeleted = walFile.delete()
                        android.util.Log.d("DatabaseModule", "WAL file deleted: $walDeleted (${walFile.absolutePath})")
                    }
                    if (shmFile.exists()) {
                        val shmDeleted = shmFile.delete()
                        android.util.Log.d("DatabaseModule", "SHM file deleted: $shmDeleted (${shmFile.absolutePath})")
                    }
                    if (journalFile.exists()) {
                        val journalDeleted = journalFile.delete()
                        android.util.Log.d("DatabaseModule", "Journal file deleted: $journalDeleted (${journalFile.absolutePath})")
                    }

                    // Now delete the main database file
                    val deleted = context.deleteDatabase(WalletDatabase.DATABASE_NAME)
                    android.util.Log.d("DatabaseModule", "Main database file deleted: $deleted")

                    // Verify all files are actually deleted
                    val dbStillExists = dbFile.exists()
                    val walStillExists = walFile.exists()
                    val shmStillExists = shmFile.exists()
                    android.util.Log.d("DatabaseModule", "After deletion - DB exists: $dbStillExists, WAL exists: $walStillExists, SHM exists: $shmStillExists")

                    if (dbStillExists || walStillExists || shmStillExists) {
                        android.util.Log.w("DatabaseModule", "Some files still exist after deletion attempt!")
                        // Try manual deletion as last resort
                        if (dbStillExists) dbFile.delete()
                        if (walStillExists) walFile.delete()
                        if (shmStillExists) shmFile.delete()
                    }

                    // Also clear the stored passphrase to generate a new one
                    clearDatabasePassphrase(context)
                    android.util.Log.i("DatabaseModule", "Database and passphrase cleared for fresh start")
                } else {
                    android.util.Log.e("DatabaseModule", "Non-encryption error occurred - rethrowing")
                    throw e
                }
            }
        } else {
            android.util.Log.d("DatabaseModule", "Database file does not exist, will be created fresh")
        }

        // Get passphrase (will be the existing one if database was verified, or new one if cleared)
        android.util.Log.d("DatabaseModule", "Getting final passphrase for database creation...")
        val finalPassphrase = getDatabasePassphrase(context)
        android.util.Log.d("DatabaseModule", "Creating SupportOpenHelperFactory with passphrase")

        // Create factory with SQLCipher default configuration (v4.x)
        // SQLCipher 4.x defaults: page_size=4096, kdf_iter=256000, HMAC_SHA512, PBKDF2_HMAC_SHA512
        val factory = SupportOpenHelperFactory(finalPassphrase)

        // Use WalletDatabase.buildDatabase which includes migrations and onCreate callback
        android.util.Log.d("DatabaseModule", "Building WalletDatabase instance...")
        val database = WalletDatabase.buildDatabase(context, factory)
        android.util.Log.i("DatabaseModule", "WalletDatabase instance created successfully")
        return database
    }

    /**
     * Clears the stored database passphrase
     */
    private fun clearDatabasePassphrase(context: Context) {
        try {
            android.util.Log.d("DatabaseModule", "Clearing stored database passphrase...")
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "encrypted_db_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            encryptedPrefs.edit().remove("db_passphrase").apply()
            android.util.Log.i("DatabaseModule", "Successfully cleared stored database passphrase")
        } catch (e: Exception) {
            android.util.Log.e("DatabaseModule", "Error clearing passphrase: ${e.javaClass.simpleName} - ${e.message}", e)
        }
    }

    /**
     * Gets the database passphrase from encrypted storage, or generates a new one if not exists
     */
    private fun getDatabasePassphrase(context: Context): ByteArray {
        try {
            android.util.Log.d("DatabaseModule", "Getting database passphrase from encrypted storage...")

            // Create master key for encryption
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            android.util.Log.d("DatabaseModule", "Master key created successfully")

            // Use EncryptedSharedPreferences to store passphrase
            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "encrypted_db_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            android.util.Log.d("DatabaseModule", "EncryptedSharedPreferences created successfully")

            // Get existing passphrase or generate new one
            val existingPassphrase = encryptedPrefs.getString("db_passphrase", null)

            return if (existingPassphrase != null) {
                // Use existing passphrase
                android.util.Log.i("DatabaseModule", "Using existing passphrase from encrypted storage")
                val decoded = android.util.Base64.decode(existingPassphrase, android.util.Base64.DEFAULT)
                android.util.Log.d("DatabaseModule", "Decoded passphrase, length: ${decoded.size} bytes")
                decoded
            } else {
                // Generate new secure passphrase
                android.util.Log.w("DatabaseModule", "No existing passphrase found, generating new one")
                val newPassphrase = ByteArray(32).apply {
                    SecureRandom().nextBytes(this)
                }
                android.util.Log.d("DatabaseModule", "Generated new passphrase, length: ${newPassphrase.size} bytes")

                // Save passphrase for future use
                val encoded = android.util.Base64.encodeToString(newPassphrase, android.util.Base64.DEFAULT)
                encryptedPrefs.edit().putString("db_passphrase", encoded).apply()
                android.util.Log.i("DatabaseModule", "New passphrase saved to encrypted storage")

                newPassphrase
            }
        } catch (e: Exception) {
            // Fallback to a hardcoded passphrase in case of error (not recommended for production)
            // This ensures app doesn't crash but should be logged
            android.util.Log.e("DatabaseModule", "Error getting passphrase: ${e.javaClass.simpleName} - ${e.message}", e)
            android.util.Log.w("DatabaseModule", "Falling back to hardcoded passphrase - THIS IS NOT SECURE!")
            return "PersonalWalletSecureKey2025".toByteArray()
        }
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
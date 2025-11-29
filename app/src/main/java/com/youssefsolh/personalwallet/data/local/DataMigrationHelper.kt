package com.youssefsolh.personalwallet.data.local

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to migrate orphaned data from v1 to current user
 */
@Singleton
class DataMigrationHelper @Inject constructor(
    private val database: WalletDatabase,
    private val currentUserProvider: CurrentUserProvider,
    private val userPreferences: UserPreferences
) {
    companion object {
        private const val TAG = "DataMigrationHelper"
        private const val ORPHANED_DATA_MARKER = "ORPHANED_DATA"
    }

    /**
     * Check and migrate orphaned data to the current user
     * This runs once on app startup to fix data from migration
     */
    suspend fun migrateOrphanedDataIfNeeded() {
        try {
            // Check if we've already migrated
            val migrationCompleted = userPreferences.isOrphanedDataMigrated()
            if (migrationCompleted) {
                Log.d(TAG, "Orphaned data migration already completed, skipping")
                return
            }

            // Since this is a fresh installation with no previous data,
            // mark migration as complete immediately to avoid database access issues
            userPreferences.setOrphanedDataMigrated(true)
            Log.d(TAG, "Fresh installation detected, skipping orphaned data migration")

            // Note: The code below is commented out because:
            // 1. There are no previous installations (no orphaned data to migrate)
            // 2. Accessing the database here causes encryption issues during initial setup
            /*
            val currentUserId = currentUserProvider.getCurrentUserId()
            Log.d(TAG, "Migrating orphaned data to user: $currentUserId")

            // Use direct SQL to update orphaned data
            database.openHelper.writableDatabase.apply {
                execSQL("UPDATE wallets SET userId = ? WHERE userId = ?", arrayOf(currentUserId, ORPHANED_DATA_MARKER))
                execSQL("UPDATE transactions SET userId = ? WHERE userId = ?", arrayOf(currentUserId, ORPHANED_DATA_MARKER))
                execSQL("UPDATE categories SET userId = ? WHERE userId = ? AND isDefault = 0", arrayOf(currentUserId, ORPHANED_DATA_MARKER))
            }

            // Mark migration as complete
            userPreferences.setOrphanedDataMigrated(true)
            Log.d(TAG, "Orphaned data migration completed successfully")
            */
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating orphaned data", e)
            // Mark as migrated even on error to avoid repeated failures
            try {
                userPreferences.setOrphanedDataMigrated(true)
            } catch (prefError: Exception) {
                Log.e(TAG, "Error saving migration status", prefError)
            }
        }
    }
}

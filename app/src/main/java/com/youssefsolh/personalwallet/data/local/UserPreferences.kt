package com.youssefsolh.personalwallet.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Theme mode options for the app
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromString(value: String): ThemeMode {
            return values().find { it.name == value } ?: SYSTEM
        }
    }
}

/**
 * Repository for user preferences using DataStore
 */
@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val DEFAULT_CURRENCY_KEY = stringPreferencesKey("default_currency")
        private val RESTORE_PROMPTED_EMAIL_KEY = stringPreferencesKey("restore_prompted_email")
        private val GUEST_USER_ID_KEY = stringPreferencesKey("guest_user_id")
        private val ORPHANED_DATA_MIGRATED_KEY = booleanPreferencesKey("orphaned_data_migrated")
    }

    /**
     * Get the current theme mode as a Flow
     */
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val themeModeString = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        ThemeMode.fromString(themeModeString)
    }

    /**
     * Get the default currency as a Flow
     */
    val defaultCurrency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_CURRENCY_KEY] ?: "USD"
    }

    /**
     * Set the theme mode
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    /**
     * Set the default currency
     */
    suspend fun setDefaultCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_CURRENCY_KEY] = currency
        }
    }

    /**
     * Check if the user has been prompted to restore backup for this email
     */
    suspend fun hasBeenPromptedForRestore(email: String): Boolean {
        val preferences = context.dataStore.data.map { it[RESTORE_PROMPTED_EMAIL_KEY] }
        return preferences.map { it == email }.map { it ?: false }.first()
    }

    /**
     * Mark that the user has been prompted to restore backup for this email
     */
    suspend fun markRestorePrompted(email: String) {
        context.dataStore.edit { preferences ->
            preferences[RESTORE_PROMPTED_EMAIL_KEY] = email
        }
    }

    /**
     * Clear the restore prompted state (for testing or when signing out)
     */
    suspend fun clearRestorePrompted() {
        context.dataStore.edit { preferences ->
            preferences.remove(RESTORE_PROMPTED_EMAIL_KEY)
        }
    }

    /**
     * Get the stored guest user ID
     */
    suspend fun getGuestUserId(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[GUEST_USER_ID_KEY]
    }

    /**
     * Set the guest user ID
     */
    suspend fun setGuestUserId(guestId: String) {
        context.dataStore.edit { preferences ->
            preferences[GUEST_USER_ID_KEY] = guestId
        }
    }

    /**
     * Clear the guest user ID
     */
    suspend fun clearGuestUserId() {
        context.dataStore.edit { preferences ->
            preferences.remove(GUEST_USER_ID_KEY)
        }
    }

    /**
     * Check if orphaned data has been migrated
     */
    suspend fun isOrphanedDataMigrated(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[ORPHANED_DATA_MIGRATED_KEY] ?: false
    }

    /**
     * Mark orphaned data as migrated
     */
    suspend fun setOrphanedDataMigrated(migrated: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ORPHANED_DATA_MIGRATED_KEY] = migrated
        }
    }
}

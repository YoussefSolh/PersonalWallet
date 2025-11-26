package com.youssefsolh.personalwallet.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
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
}

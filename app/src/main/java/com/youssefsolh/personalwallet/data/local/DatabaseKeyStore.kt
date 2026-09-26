package com.youssefsolh.personalwallet.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Holds the random SQLCipher passphrase in Keystore-backed encrypted preferences.
 *
 * There is deliberately no hardcoded fallback: if the key cannot be read, the encrypted
 * prefs are reset and a new key is generated. The old database can then no longer be
 * opened and is quarantined by [DatabaseRecovery] rather than deleted.
 */
object DatabaseKeyStore {
    private const val TAG = "DatabaseKeyStore"
    const val PREFS_NAME = "encrypted_db_prefs"
    private const val KEY_PASSPHRASE = "db_passphrase"

    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs = try {
            openPrefs(context)
        } catch (e: Exception) {
            // Typically the Keystore master key is gone (e.g. prefs restored onto another
            // device). The stored passphrase is unrecoverable either way.
            Log.e(TAG, "Encrypted key storage unreadable; resetting it", e)
            context.deleteSharedPreferences(PREFS_NAME)
            openPrefs(context) // If this fails too, fail loudly rather than use a weak key
        }

        prefs.getString(KEY_PASSPHRASE, null)?.let { stored ->
            return Base64.decode(stored, Base64.DEFAULT)
        }

        Log.i(TAG, "Generating new database passphrase")
        val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val saved = prefs.edit()
            .putString(KEY_PASSPHRASE, Base64.encodeToString(passphrase, Base64.NO_WRAP))
            .commit()
        check(saved) { "Failed to persist database passphrase" }
        return passphrase
    }

    private fun openPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}

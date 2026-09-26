package com.youssefsolh.personalwallet.data.local

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Handles a database that exists but cannot be decrypted with the current key.
 *
 * Instead of deleting it, the files are moved aside so nothing is destroyed, and a flag
 * is recorded so the UI can tell the user and point them to restoring a Drive backup.
 */
object DatabaseRecovery {
    private const val TAG = "DatabaseRecovery"
    private const val PREFS_NAME = "db_recovery"
    private const val KEY_QUARANTINED_FILE = "quarantined_file"

    private val SUFFIXES = listOf("", "-wal", "-shm", "-journal")

    fun isUndecryptable(error: Throwable): Boolean {
        return generateSequence(error) { it.cause }.any { cause ->
            val message = cause.message ?: return@any false
            message.contains("file is not a database", ignoreCase = true) ||
                message.contains("file is encrypted", ignoreCase = true) ||
                message.contains("hmac", ignoreCase = true)
        }
    }

    /** Moves the database files aside and returns the new main file name. */
    fun quarantine(context: Context, databaseName: String): String {
        val dbFile = context.getDatabasePath(databaseName)
        val quarantinedName = "$databaseName.unreadable-${System.currentTimeMillis()}"
        SUFFIXES.forEach { suffix ->
            val source = File(dbFile.path + suffix)
            if (source.exists()) {
                val target = File(dbFile.parentFile, quarantinedName + suffix)
                if (!source.renameTo(target)) {
                    throw IllegalStateException("Could not move ${source.name} aside")
                }
            }
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_QUARANTINED_FILE, quarantinedName)
            .apply()
        Log.w(TAG, "Unreadable database moved to $quarantinedName")
        return quarantinedName
    }

    /** Name of a database moved aside that the user has not been told about yet, if any. */
    fun pendingNotice(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_QUARANTINED_FILE, null)
    }

    fun acknowledgeNotice(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_QUARANTINED_FILE)
            .apply()
    }
}

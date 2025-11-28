package com.youssefsolh.personalwallet.data.remote.auth

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.youssefsolh.personalwallet.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AuthService"
    }

    /**
     * Google Sign-In Client configured for authentication
     *
     * Configuration:
     * - requestIdToken: Required for backend authentication (from strings.xml)
     * - requestEmail: To get user's email address
     * - requestProfile: To get user's name and profile picture
     */
    val googleSignInClient: GoogleSignInClient by lazy {
        try {
            val webClientId = context.getString(com.youssefsolh.personalwallet.R.string.default_web_client_id)

            // Check if the web client ID is still the placeholder
            if (webClientId == "YOUR_WEB_CLIENT_ID_HERE") {
                Log.w(TAG, "Google Sign-In not configured: Web Client ID is still placeholder")
            }

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .requestProfile()
                .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                .build()

            GoogleSignIn.getClient(context, gso)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating GoogleSignInClient", e)
            throw e
        }
    }

    /**
     * Get the currently signed-in Google account, if any
     * @return GoogleSignInAccount if user is signed in, null otherwise
     */
    fun getCurrentGoogleAccount(): GoogleSignInAccount? {
        return try {
            GoogleSignIn.getLastSignedInAccount(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current Google account", e)
            null
        }
    }

    /**
     * Verify the ID token and get account information
     * @param idToken The ID token from Google Sign-In
     * @return GoogleSignInAccount if verification succeeds
     * @throws Exception if verification fails
     */
    fun verifyIdToken(idToken: String): GoogleSignInAccount? {
        // In a production app, you would verify the ID token with your backend here
        // For now, we just get the current account
        val account = getCurrentGoogleAccount()

        if (account?.idToken != idToken) {
            Log.w(TAG, "ID token mismatch")
            return null
        }

        return account
    }

    /**
     * Convert GoogleSignInAccount to User domain model
     * @param account The Google account to convert
     * @return User domain model
     */
    fun accountToUser(account: GoogleSignInAccount): User {
        return User(
            id = account.id ?: account.email?.hashCode()?.toString() ?: "",
            email = account.email ?: "",
            displayName = account.displayName ?: "Google User",
            photoUrl = account.photoUrl?.toString(),
            isGuest = false
        )
    }

    /**
     * Sign out from Google Sign-In
     * Clears the cached account information
     */
    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
            Log.d(TAG, "User signed out successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out", e)
            throw e
        }
    }

    /**
     * Revoke access to the app
     * This will disconnect the app from the user's Google account
     */
    suspend fun revokeAccess() {
        try {
            googleSignInClient.revokeAccess().await()
            Log.d(TAG, "Access revoked successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error revoking access", e)
            throw e
        }
    }

    /**
     * Check if Google Sign-In is properly configured
     * @return true if configured, false if using placeholder
     */
    fun isGoogleSignInConfigured(): Boolean {
        val webClientId = context.getString(com.youssefsolh.personalwallet.R.string.default_web_client_id)
        return webClientId != "YOUR_WEB_CLIENT_ID_HERE"
    }
}

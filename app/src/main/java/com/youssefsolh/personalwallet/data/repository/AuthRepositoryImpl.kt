package com.youssefsolh.personalwallet.data.repository

import android.util.Log
import com.youssefsolh.personalwallet.data.remote.auth.AuthService
import com.youssefsolh.personalwallet.domain.model.User
import com.youssefsolh.personalwallet.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    private val _currentUser = MutableStateFlow<User?>(null)

    init {
        // Check if user is already signed in
        try {
            authService.getCurrentGoogleAccount()?.let { account ->
                val user = authService.accountToUser(account)
                _currentUser.value = user
                Log.d(TAG, "User already signed in: ${account.email}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking existing sign-in", e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            Log.d(TAG, "Attempting Google Sign-In with ID token")

            // Check if Google Sign-In is configured
            if (!authService.isGoogleSignInConfigured()) {
                val errorMsg = "Google Sign-In not configured. Please add your Web Client ID to strings.xml"
                Log.e(TAG, errorMsg)
                return Result.failure(Exception(errorMsg))
            }

            // Verify the ID token and get account
            val account = authService.verifyIdToken(idToken)

            if (account != null && account.idToken != null) {
                // Token is valid, convert to user and save
                val user = authService.accountToUser(account)
                _currentUser.value = user
                Log.d(TAG, "Successfully signed in: ${user.email}")
                Result.success(user)
            } else {
                // Try to get current account as fallback
                val fallbackAccount = authService.getCurrentGoogleAccount()
                if (fallbackAccount != null) {
                    val user = authService.accountToUser(fallbackAccount)
                    _currentUser.value = user
                    Log.d(TAG, "Signed in using fallback account: ${user.email}")
                    Result.success(user)
                } else {
                    val errorMsg = "Failed to get Google account. Please try signing in again."
                    Log.e(TAG, errorMsg)
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during Google Sign-In", e)
            Result.failure(e)
        }
    }

    override suspend fun signInAsGuest(): Result<User> {
        return try {
            Log.d(TAG, "Signing in as guest")

            val guestUser = User(
                id = "guest_${UUID.randomUUID()}",
                email = "guest@personalwallet.com",
                displayName = "Guest User",
                photoUrl = null,
                isGuest = true
            )
            _currentUser.value = guestUser
            Log.d(TAG, "Guest sign-in successful")
            Result.success(guestUser)
        } catch (e: Exception) {
            Log.e(TAG, "Error during guest sign-in", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            Log.d(TAG, "Signing out user: ${_currentUser.value?.email}")

            val wasGuest = _currentUser.value?.isGuest ?: false

            // Only sign out from Google if not a guest
            if (!wasGuest) {
                authService.signOut()
            }

            _currentUser.value = null
            Log.d(TAG, "Sign out successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign out", e)
            // Even if sign out fails, clear local state
            _currentUser.value = null
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): Flow<User?> {
        return _currentUser.asStateFlow()
    }

    override fun isUserSignedIn(): Flow<Boolean> {
        return _currentUser.map { it != null }
    }
}

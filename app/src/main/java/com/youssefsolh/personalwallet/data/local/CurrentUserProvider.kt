package com.youssefsolh.personalwallet.data.local

import com.youssefsolh.personalwallet.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides the current user ID for data isolation
 */
@Singleton
class CurrentUserProvider @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) {
    companion object {
        private var guestUserId: String? = null
    }

    /**
     * Get the current user's ID for database filtering
     * Returns unique guest ID for guest users, email for Google users
     */
    suspend fun getCurrentUserId(): String {
        val user = authRepository.getCurrentUser().first()
        return if (user == null || user.isGuest) {
            getGuestUserId()
        } else {
            user.id
        }
    }

    /**
     * Get or create a unique guest user ID
     * This ID persists per device until the user signs in with Google
     */
    private suspend fun getGuestUserId(): String {
        // Check in-memory cache first
        if (guestUserId != null) {
            return guestUserId!!
        }

        // Check if we have a stored guest ID
        val storedGuestId = userPreferences.getGuestUserId()
        if (storedGuestId != null) {
            guestUserId = storedGuestId
            return storedGuestId
        }

        // Generate a new unique guest ID
        val newGuestId = "guest_${UUID.randomUUID()}"
        userPreferences.setGuestUserId(newGuestId)
        guestUserId = newGuestId
        return newGuestId
    }

    /**
     * Clear the guest user ID (called when signing in with Google)
     */
    suspend fun clearGuestUserId() {
        guestUserId = null
        userPreferences.clearGuestUserId()
    }
}

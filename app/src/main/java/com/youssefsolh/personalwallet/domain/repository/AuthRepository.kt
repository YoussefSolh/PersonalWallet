package com.youssefsolh.personalwallet.domain.repository

import com.youssefsolh.personalwallet.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInAsGuest(): Result<User>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): Flow<User?>
    fun isUserSignedIn(): Flow<Boolean>
}

package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.data.local.UserPreferences
import com.youssefsolh.personalwallet.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.signOut().also {
            // Clear the restore prompted state so user will be prompted again on next sign-in
            if (it.isSuccess) {
                userPreferences.clearRestorePrompted()
            }
        }
    }
}

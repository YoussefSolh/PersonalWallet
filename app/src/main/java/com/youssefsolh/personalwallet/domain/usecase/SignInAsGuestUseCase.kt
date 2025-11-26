package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.User
import com.youssefsolh.personalwallet.domain.repository.AuthRepository
import javax.inject.Inject

class SignInAsGuestUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User> {
        return authRepository.signInAsGuest()
    }
}

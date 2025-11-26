package com.youssefsolh.personalwallet.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.youssefsolh.personalwallet.data.remote.auth.AuthService
import com.youssefsolh.personalwallet.domain.model.User
import com.youssefsolh.personalwallet.domain.usecase.SignInAsGuestUseCase
import com.youssefsolh.personalwallet.domain.usecase.SignInWithGoogleUseCase
import com.youssefsolh.personalwallet.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signInAsGuestUseCase: SignInAsGuestUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun getGoogleSignInIntent(context: Context): Intent {
        return authService.googleSignInClient.signInIntent
    }

    fun handleGoogleSignInError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    fun signInWithGoogleAccount(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val user = authService.accountToUser(account)
                _uiState.value = AuthUiState(
                    user = user,
                    isLoading = false,
                    isSignedIn = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to sign in with Google"
                )
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            signInWithGoogleUseCase(idToken).fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState(
                        user = user,
                        isLoading = false,
                        isSignedIn = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to sign in"
                    )
                }
            )
        }
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            signInAsGuestUseCase().fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState(
                        user = user,
                        isLoading = false,
                        isSignedIn = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to continue as guest"
                    )
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase().onSuccess {
                _uiState.value = AuthUiState()
            }
        }
    }
}

data class AuthUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val errorMessage: String? = null
)

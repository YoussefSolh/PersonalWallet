package com.youssefsolh.personalwallet.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.youssefsolh.personalwallet.data.local.UserPreferences
import com.youssefsolh.personalwallet.data.remote.auth.AuthService
import com.youssefsolh.personalwallet.domain.model.User
import com.youssefsolh.personalwallet.domain.repository.BackupRepository
import com.youssefsolh.personalwallet.domain.repository.DriveBackupInfo
import com.youssefsolh.personalwallet.domain.usecase.RestoreDataUseCase
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
    private val authService: AuthService,
    private val backupRepository: BackupRepository,
    private val restoreDataUseCase: RestoreDataUseCase,
    private val userPreferences: UserPreferences
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

                // Check for existing backups after successful sign-in
                checkForExistingBackups(user.email)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to sign in with Google"
                )
            }
        }
    }

    private suspend fun checkForExistingBackups(email: String) {
        // Skip if user is guest or email is empty
        if (email.isBlank()) return

        // Check if we've already prompted this user
        val alreadyPrompted = userPreferences.hasBeenPromptedForRestore(email)
        if (alreadyPrompted) return

        _uiState.value = _uiState.value.copy(isCheckingBackups = true)

        // List available backups from Drive
        backupRepository.listDriveBackups().fold(
            onSuccess = { backups ->
                if (backups.isNotEmpty()) {
                    // Show restore prompt if backups exist
                    _uiState.value = _uiState.value.copy(
                        isCheckingBackups = false,
                        availableBackups = backups,
                        showRestorePrompt = true
                    )
                } else {
                    // No backups found, mark as prompted
                    userPreferences.markRestorePrompted(email)
                    _uiState.value = _uiState.value.copy(isCheckingBackups = false)
                }
            },
            onFailure = { error ->
                // Failed to check backups (maybe Drive API disabled), just continue
                android.util.Log.e("AuthViewModel", "Failed to check for backups", error)
                _uiState.value = _uiState.value.copy(isCheckingBackups = false)
            }
        )
    }

    fun dismissRestorePrompt() {
        viewModelScope.launch {
            // User declined restore, mark as prompted
            _uiState.value.user?.email?.let { email ->
                userPreferences.markRestorePrompted(email)
            }
            _uiState.value = _uiState.value.copy(
                showRestorePrompt = false,
                availableBackups = null
            )
        }
    }

    fun restoreFromBackup(backupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRestoring = true,
                showRestorePrompt = false
            )

            restoreDataUseCase(backupId).fold(
                onSuccess = {
                    // Mark as prompted after successful restore
                    _uiState.value.user?.email?.let { email ->
                        userPreferences.markRestorePrompted(email)
                    }
                    _uiState.value = _uiState.value.copy(
                        isRestoring = false,
                        availableBackups = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isRestoring = false,
                        errorMessage = "Failed to restore backup: ${error.message}"
                    )
                }
            )
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
    val errorMessage: String? = null,
    val availableBackups: List<DriveBackupInfo>? = null,
    val showRestorePrompt: Boolean = false,
    val isCheckingBackups: Boolean = false,
    val isRestoring: Boolean = false
)

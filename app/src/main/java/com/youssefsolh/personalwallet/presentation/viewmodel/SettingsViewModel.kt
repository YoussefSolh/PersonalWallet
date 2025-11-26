package com.youssefsolh.personalwallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youssefsolh.personalwallet.data.local.ThemeMode
import com.youssefsolh.personalwallet.data.local.UserPreferences
import com.youssefsolh.personalwallet.domain.usecase.BackupDataUseCase
import com.youssefsolh.personalwallet.domain.usecase.RestoreDataUseCase
import com.youssefsolh.personalwallet.domain.usecase.SignOutUseCase
import com.youssefsolh.personalwallet.domain.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupDataUseCase: BackupDataUseCase,
    private val restoreDataUseCase: RestoreDataUseCase,
    private val backupRepository: BackupRepository,
    private val signOutUseCase: SignOutUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadLastBackupTime()
        loadPreferences()
    }

    private fun loadLastBackupTime() {
        viewModelScope.launch {
            val timestamp = backupRepository.getLastBackupTimestamp()
            _uiState.value = _uiState.value.copy(lastBackupTimestamp = timestamp)
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            combine(
                userPreferences.themeMode,
                userPreferences.defaultCurrency
            ) { themeMode, defaultCurrency ->
                _uiState.value = _uiState.value.copy(
                    themeMode = themeMode,
                    defaultCurrency = defaultCurrency
                )
            }.collect {}
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBackingUp = true, errorMessage = null)

            backupDataUseCase().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isBackingUp = false,
                        successMessage = "Backup created successfully",
                        lastBackupTimestamp = System.currentTimeMillis()
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isBackingUp = false,
                        errorMessage = error.message ?: "Backup failed"
                    )
                }
            )
        }
    }

    fun restoreBackup(fileId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoring = true, errorMessage = null)

            restoreDataUseCase(fileId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isRestoring = false,
                        successMessage = "Data restored successfully"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isRestoring = false,
                        errorMessage = error.message ?: "Restore failed"
                    )
                }
            )
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            signOutUseCase().onSuccess {
                onSuccess()
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}

data class SettingsUiState(
    val lastBackupTimestamp: Long? = null,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val biometricEnabled: Boolean = false,
    val defaultCurrency: String = "USD",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

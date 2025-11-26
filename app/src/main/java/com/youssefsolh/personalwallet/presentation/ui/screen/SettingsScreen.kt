package com.youssefsolh.personalwallet.presentation.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.youssefsolh.personalwallet.data.local.ThemeMode
import com.youssefsolh.personalwallet.presentation.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToDebts: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showBackupDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages
            uiState.successMessage?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            SettingsItem(
                title = "Theme",
                subtitle = when (uiState.themeMode) {
                    ThemeMode.LIGHT -> "Light mode"
                    ThemeMode.DARK -> "Dark mode"
                    ThemeMode.SYSTEM -> "Follow system"
                },
                onClick = { showThemeDialog = true }
            )
            HorizontalDivider()

            SettingsItem(
                title = "Categories",
                subtitle = "Manage transaction categories",
                onClick = onNavigateToCategories
            )
            HorizontalDivider()

            SettingsItem(
                title = "Debts",
                subtitle = "View and manage active debts",
                onClick = onNavigateToDebts
            )
            HorizontalDivider()

            SettingsItem(
                title = "Reports & Analytics",
                subtitle = "View spending reports and insights",
                onClick = onNavigateToReports
            )
            HorizontalDivider()

            SettingsItem(
                title = "Backup & Sync",
                subtitle = uiState.lastBackupTimestamp?.let {
                    "Last backup: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(it))}"
                } ?: "No backup yet",
                onClick = { showBackupDialog = true }
            )
            HorizontalDivider()

            SettingsItem(
                title = "Currency",
                subtitle = "Default: ${uiState.defaultCurrency}"
            )
            HorizontalDivider()

            SettingsItem(
                title = "Sign Out",
                subtitle = "Sign out of your account",
                onClick = { showSignOutDialog = true }
            )
            HorizontalDivider()

            SettingsItem(
                title = "About",
                subtitle = "Version 1.0.0"
            )

            // Backup Dialog
            if (showBackupDialog) {
                AlertDialog(
                    onDismissRequest = { showBackupDialog = false },
                    title = { Text("Backup & Restore") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Backup your data to Google Drive")
                            if (uiState.isBackingUp) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.createBackup()
                            },
                            enabled = !uiState.isBackingUp
                        ) {
                            Text("Backup Now")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBackupDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Sign Out Dialog
            if (showSignOutDialog) {
                AlertDialog(
                    onDismissRequest = { showSignOutDialog = false },
                    title = { Text("Sign Out") },
                    text = { Text("Are you sure you want to sign out?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.signOut(onSignOut)
                                showSignOutDialog = false
                            }
                        ) {
                            Text("Sign Out")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSignOutDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Theme Dialog
            if (showThemeDialog) {
                AlertDialog(
                    onDismissRequest = { showThemeDialog = false },
                    title = { Text("Choose Theme") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeOption(
                                title = "Light",
                                subtitle = "Always use light theme",
                                selected = uiState.themeMode == ThemeMode.LIGHT,
                                onClick = {
                                    viewModel.setThemeMode(ThemeMode.LIGHT)
                                    showThemeDialog = false
                                }
                            )
                            ThemeOption(
                                title = "Dark",
                                subtitle = "Always use dark theme",
                                selected = uiState.themeMode == ThemeMode.DARK,
                                onClick = {
                                    viewModel.setThemeMode(ThemeMode.DARK)
                                    showThemeDialog = false
                                }
                            )
                            ThemeOption(
                                title = "System",
                                subtitle = "Follow system settings",
                                selected = uiState.themeMode == ThemeMode.SYSTEM,
                                onClick = {
                                    viewModel.setThemeMode(ThemeMode.SYSTEM)
                                    showThemeDialog = false
                                }
                            )
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showThemeDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ThemeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

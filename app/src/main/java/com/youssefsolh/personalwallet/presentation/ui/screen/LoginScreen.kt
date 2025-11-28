package com.youssefsolh.personalwallet.presentation.ui.screen

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.youssefsolh.personalwallet.presentation.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                // Sign in with the account directly, no idToken needed for basic auth
                viewModel.signInWithGoogleAccount(account)
            } else {
                viewModel.handleGoogleSignInError("Failed to retrieve account information")
            }
        } catch (e: ApiException) {
            // Log the error for debugging
            android.util.Log.e("LoginScreen", "Google Sign-In failed", e)
            android.util.Log.e("LoginScreen", "Error code: ${e.statusCode}")
            android.util.Log.e("LoginScreen", "Status message: ${e.statusMessage}")

            when (e.statusCode) {
                12501 -> {
                    // User cancelled - this is normal, don't show error
                    // Just stay on login screen silently
                }
                12500 -> {
                    viewModel.handleGoogleSignInError("Google Sign-In is not configured. Please use Guest mode.\n\nTo set up Google Sign-In:\n1. Create a Google Cloud project\n2. Enable Google Sign-In API\n3. Add SHA-1 fingerprint\n4. Add Web Client ID to strings.xml")
                }
                10 -> {
                    // DEVELOPER_ERROR - usually means SHA-1 not configured
                    viewModel.handleGoogleSignInError("Google Sign-In setup incomplete. SHA-1 fingerprint may not be registered.\n\nPlease use Guest mode for now.")
                }
                else -> {
                    viewModel.handleGoogleSignInError("Sign-in error (${e.statusCode}): ${e.statusMessage ?: "Unknown error"}\n\nPlease use Guest mode.")
                }
            }
        }
    }

    // Navigate on successful login
    LaunchedEffect(uiState.isSignedIn, uiState.showRestorePrompt, uiState.isCheckingBackups, uiState.isRestoring) {
        if (uiState.isSignedIn && !uiState.showRestorePrompt && !uiState.isCheckingBackups && !uiState.isRestoring) {
            onLoginSuccess()
        }
    }

    // Restore backup dialog
    val availableBackups = uiState.availableBackups
    if (uiState.showRestorePrompt && availableBackups != null) {
        RestoreBackupDialog(
            backups = availableBackups,
            onRestore = { backupId ->
                viewModel.restoreFromBackup(backupId)
            },
            onDismiss = {
                viewModel.dismissRestorePrompt()
            }
        )
    }

    // Show loading overlay when restoring
    if (uiState.isRestoring) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Restoring Backup") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Please wait while we restore your data...")
                }
            },
            confirmButton = { }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Personal Wallet",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Welcome to your personal wallet app",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Error Message
        if (uiState.errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠️ Sign-In Error",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = uiState.errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Button(
            onClick = {
                try {
                    val signInIntent = viewModel.getGoogleSignInIntent(context)
                    googleSignInLauncher.launch(signInIntent)
                } catch (e: Exception) {
                    // If Google Sign-In fails (no account on device), show error and suggest guest mode
                    viewModel.handleGoogleSignInError("No Google account found on this device. Please use Guest mode or add a Google account in your device settings.")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign in with Google")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.signInAsGuest() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !uiState.isLoading
        ) {
            Text("Continue as Guest")
        }
    }
}

@Composable
private fun RestoreBackupDialog(
    backups: List<com.youssefsolh.personalwallet.domain.repository.DriveBackupInfo>,
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()) }
    val mostRecentBackup = backups.maxByOrNull { it.timestamp }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Backup Found!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "We found ${backups.size} backup${if (backups.size > 1) "s" else ""} in your Google Drive.",
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (mostRecentBackup != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Most Recent Backup:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dateFormat.format(Date(mostRecentBackup.timestamp)),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${mostRecentBackup.size / 1024} KB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Would you like to restore your data from this backup?",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    mostRecentBackup?.let { onRestore(it.id) }
                }
            ) {
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}
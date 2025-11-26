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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) {
            onLoginSuccess()
        }
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
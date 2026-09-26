package com.youssefsolh.personalwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.youssefsolh.personalwallet.data.local.DatabaseRecovery
import com.youssefsolh.personalwallet.data.local.ThemeMode
import com.youssefsolh.personalwallet.data.local.UserPreferences
import com.youssefsolh.personalwallet.data.local.WalletDatabase
import com.youssefsolh.personalwallet.presentation.navigation.WalletNavigation
import com.youssefsolh.personalwallet.ui.theme.PersonalWalletTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    // Injected so the database is opened (and any recovery recorded) before the UI starts
    @Inject
    lateinit var walletDatabase: WalletDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by userPreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            var recoveryNotice by remember { mutableStateOf(DatabaseRecovery.pendingNotice(this)) }

            PersonalWalletTheme(themeMode = themeMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WalletNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                if (recoveryNotice != null) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("Wallet data couldn't be unlocked") },
                        text = {
                            Text(
                                "Your saved wallet data could not be decrypted on this device, " +
                                    "so the app started with an empty wallet. The old data was kept, " +
                                    "not deleted. If you have a Google Drive backup, restore it from Settings."
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                DatabaseRecovery.acknowledgeNotice(this@MainActivity)
                                recoveryNotice = null
                            }) { Text("OK") }
                        }
                    )
                }
            }
        }
    }
}
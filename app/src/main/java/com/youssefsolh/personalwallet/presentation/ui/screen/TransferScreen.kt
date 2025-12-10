package com.youssefsolh.personalwallet.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.presentation.viewmodel.TransferViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    fromWalletId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: TransferViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFromWalletDropdown by remember { mutableStateOf(false) }
    var showToWalletDropdown by remember { mutableStateOf(false) }

    // Pre-select fromWallet if provided
    LaunchedEffect(fromWalletId, uiState.wallets) {
        if (fromWalletId != null && uiState.fromWallet == null) {
            uiState.wallets.find { it.id == fromWalletId }?.let {
                viewModel.onFromWalletSelected(it)
            }
        }
    }

    // Helper function to get currency symbol
    fun getCurrencySymbol(currencyCode: String): String {
        return uiState.currencies.find { it.code == currencyCode }?.symbol ?: currencyCode
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Money") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // From Wallet Selector
            Text(
                text = "From Wallet",
                style = MaterialTheme.typography.titleMedium
            )

            ExposedDropdownMenuBox(
                expanded = showFromWalletDropdown,
                onExpandedChange = { showFromWalletDropdown = it }
            ) {
                OutlinedTextField(
                    value = uiState.fromWallet?.name ?: "Select wallet",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, null)
                    },
                    colors = OutlinedTextFieldDefaults.colors()
                )

                ExposedDropdownMenu(
                    expanded = showFromWalletDropdown,
                    onDismissRequest = { showFromWalletDropdown = false }
                ) {
                    uiState.wallets.forEach { wallet ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(wallet.name)
                                    Text(
                                        text = "${getCurrencySymbol(wallet.currency)}${wallet.balance}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                viewModel.onFromWalletSelected(wallet)
                                showFromWalletDropdown = false
                            }
                        )
                    }
                }
            }

            // To Wallet Selector
            Text(
                text = "To Wallet",
                style = MaterialTheme.typography.titleMedium
            )

            ExposedDropdownMenuBox(
                expanded = showToWalletDropdown,
                onExpandedChange = { showToWalletDropdown = it }
            ) {
                OutlinedTextField(
                    value = uiState.toWallet?.name ?: "Select wallet",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, null)
                    },
                    colors = OutlinedTextFieldDefaults.colors()
                )

                ExposedDropdownMenu(
                    expanded = showToWalletDropdown,
                    onDismissRequest = { showToWalletDropdown = false }
                ) {
                    uiState.wallets
                        .filter { it.id != uiState.fromWallet?.id }
                        .forEach { wallet ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(wallet.name)
                                        Text(
                                            text = "${getCurrencySymbol(wallet.currency)}${wallet.balance}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.onToWalletSelected(wallet)
                                    showToWalletDropdown = false
                                }
                            )
                        }
                }
            }

            // Amount Input
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::onAmountChanged,
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = {
                    Text(uiState.fromWallet?.currency?.let { getCurrencySymbol(it) } ?: "")
                },
                singleLine = true
            )

            // Show converted amount if currencies differ
            if (uiState.convertedAmount != null && uiState.toWallet != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Destination will receive:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${getCurrencySymbol(uiState.toWallet!!.currency)}${uiState.convertedAmount}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "≈",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Description Input
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // Debt Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mark as Debt",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Track this transfer as a debt to be settled later",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isDebt,
                    onCheckedChange = viewModel::onDebtToggled
                )
            }

            // Error Message
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Transfer Button
            Button(
                onClick = { viewModel.onTransfer(onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Transfer")
                }
            }
        }
    }
}

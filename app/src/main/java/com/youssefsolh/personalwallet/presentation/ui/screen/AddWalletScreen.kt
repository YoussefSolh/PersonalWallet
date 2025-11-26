package com.youssefsolh.personalwallet.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youssefsolh.personalwallet.presentation.viewmodel.AddWalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWalletScreen(
    walletId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddWalletViewModel = hiltViewModel()
) {
    val isEditMode = walletId != null

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var initialBalance by remember { mutableStateOf("") }
    var balanceError by remember { mutableStateOf<String?>(null) }
    var currency by remember { mutableStateOf("USD") }
    var currencyError by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()

    // Load wallet data if editing
    LaunchedEffect(walletId) {
        walletId?.let {
            viewModel.loadWallet(it)
        }
    }

    // Populate form with wallet data when loaded
    LaunchedEffect(uiState.wallet) {
        uiState.wallet?.let { wallet ->
            name = wallet.name
            initialBalance = wallet.balance.toString()
            currency = wallet.currency
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Wallet" else "Add Wallet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("Wallet Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                placeholder = { Text("e.g., Cash, Bank Account") }
            )

            OutlinedTextField(
                value = initialBalance,
                onValueChange = {
                    initialBalance = it
                    balanceError = null
                },
                label = { Text(if (isEditMode) "Current Balance" else "Initial Balance") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text("$") },
                isError = balanceError != null,
                supportingText = balanceError?.let { { Text(it) } },
                placeholder = { Text("0.00") }
            )

            OutlinedTextField(
                value = currency,
                onValueChange = {
                    currency = it.uppercase()
                    currencyError = null
                },
                label = { Text("Currency") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = currencyError != null,
                supportingText = currencyError?.let { { Text(it) } },
                placeholder = { Text("USD") }
            )

            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    // Validate all fields
                    var hasError = false

                    if (name.isBlank()) {
                        nameError = "Wallet name is required"
                        hasError = true
                    } else if (name.length < 2) {
                        nameError = "Wallet name must be at least 2 characters"
                        hasError = true
                    }

                    if (initialBalance.isBlank()) {
                        balanceError = "Initial balance is required"
                        hasError = true
                    } else {
                        val balanceValue = initialBalance.toDoubleOrNull()
                        when {
                            balanceValue == null -> {
                                balanceError = "Please enter a valid number"
                                hasError = true
                            }
                            balanceValue < 0 -> {
                                balanceError = "Balance cannot be negative"
                                hasError = true
                            }
                        }
                    }

                    if (currency.isBlank()) {
                        currencyError = "Currency is required"
                        hasError = true
                    } else if (currency.length != 3) {
                        currencyError = "Currency must be 3 letters (e.g., USD, EUR)"
                        hasError = true
                    }

                    if (!hasError) {
                        if (isEditMode && walletId != null) {
                            viewModel.updateWallet(
                                id = walletId,
                                name = name.trim(),
                                balance = initialBalance.toDouble(),
                                currency = currency.uppercase()
                            )
                        } else {
                            viewModel.createWallet(
                                name = name.trim(),
                                initialBalance = initialBalance.toDouble(),
                                currency = currency.uppercase()
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditMode) "Update Wallet" else "Create Wallet")
                }
            }
        }
    }
}

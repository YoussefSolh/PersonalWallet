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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.youssefsolh.personalwallet.domain.model.Currency
import com.youssefsolh.personalwallet.presentation.ui.common.CurrencyDropdown
import com.youssefsolh.personalwallet.presentation.viewmodel.AddWalletViewModel
import com.youssefsolh.personalwallet.presentation.viewmodel.CurrencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWalletScreen(
    walletId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddWalletViewModel = hiltViewModel(),
    currencyViewModel: CurrencyViewModel = hiltViewModel()
) {
    val isEditMode = walletId != null

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var initialBalance by remember { mutableStateOf("") }
    var balanceError by remember { mutableStateOf<String?>(null) }
    var selectedCurrency by remember { mutableStateOf<Currency?>(null) }
    var currencyError by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()
    val currencyUiState by currencyViewModel.uiState.collectAsStateWithLifecycle()

    // Load wallet data if editing
    LaunchedEffect(walletId) {
        walletId?.let {
            viewModel.loadWallet(it)
        }
    }

    // Set default currency when currencies are loaded (only if not editing)
    LaunchedEffect(currencyUiState.defaultCurrency) {
        if (!isEditMode && selectedCurrency == null) {
            currencyUiState.defaultCurrency?.let {
                selectedCurrency = it
            }
        }
    }

    // Populate form with wallet data when loaded
    LaunchedEffect(uiState.wallet, currencyUiState.currencies) {
        uiState.wallet?.let { wallet ->
            name = wallet.name
            initialBalance = wallet.balance.toString()
            // Find the currency object from the code
            selectedCurrency = currencyUiState.currencies.find { it.code == wallet.currency }
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
                prefix = { Text(selectedCurrency?.symbol ?: "$") },
                isError = balanceError != null,
                supportingText = balanceError?.let { { Text(it) } },
                placeholder = { Text("0.00") }
            )

            CurrencyDropdown(
                selectedCurrency = selectedCurrency,
                currencies = currencyUiState.currencies,
                onCurrencySelected = {
                    selectedCurrency = it
                    currencyError = null
                },
                label = "Currency",
                modifier = Modifier.fillMaxWidth(),
                isError = currencyError != null,
                supportingText = currencyError
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

                    if (selectedCurrency == null) {
                        currencyError = "Please select a currency"
                        hasError = true
                    }

                    if (!hasError) {
                        val currencyCode = selectedCurrency?.code ?: "USD"
                        if (isEditMode && walletId != null) {
                            viewModel.updateWallet(
                                id = walletId,
                                name = name.trim(),
                                balance = initialBalance.toDouble(),
                                currency = currencyCode
                            )
                        } else {
                            viewModel.createWallet(
                                name = name.trim(),
                                initialBalance = initialBalance.toDouble(),
                                currency = currencyCode
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

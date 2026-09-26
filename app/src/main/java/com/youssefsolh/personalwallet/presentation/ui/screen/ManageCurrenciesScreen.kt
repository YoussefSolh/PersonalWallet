package com.youssefsolh.personalwallet.presentation.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.youssefsolh.personalwallet.domain.model.Currency
import com.youssefsolh.personalwallet.presentation.viewmodel.CurrencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCurrenciesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddCurrency: () -> Unit,
    onNavigateToEditCurrency: (String) -> Unit,
    viewModel: CurrencyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currencyToDelete by remember { mutableStateOf<Currency?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Currencies") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddCurrency) {
                Icon(Icons.Default.Add, "Add Currency")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.currencies, key = { it.code }) { currency ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToEditCurrency(currency.code) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${currency.code} (${currency.symbol})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currency.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                val defaultCode = uiState.defaultCurrency?.code ?: "USD"
                                Text(
                                    text = if (currency.isDefault) "Default currency"
                                    else "1 $defaultCode = ${currency.exchangeRateToDefault.stripTrailingZeros().toPlainString()} ${currency.code}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!currency.isDefault && !currency.isSystemCurrency) {
                                IconButton(onClick = { currencyToDelete = currency }) {
                                    Icon(Icons.Default.Delete, "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    currencyToDelete?.let { currency ->
        AlertDialog(
            onDismissRequest = { currencyToDelete = null },
            title = { Text("Delete Currency") },
            text = { Text("Delete ${currency.code}? Currencies used by a wallet cannot be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCurrency(currency.code)
                    currencyToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { currencyToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

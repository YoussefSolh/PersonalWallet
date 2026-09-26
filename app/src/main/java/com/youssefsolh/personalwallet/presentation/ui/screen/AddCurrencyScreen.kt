package com.youssefsolh.personalwallet.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.youssefsolh.personalwallet.presentation.viewmodel.CurrencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCurrencyScreen(
    onNavigateBack: () -> Unit,
    currencyCode: String? = null,
    viewModel: CurrencyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditMode = !currencyCode.isNullOrBlank()

    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var symbol by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }

    LaunchedEffect(currencyCode) {
        if (isEditMode) viewModel.loadCurrency(currencyCode!!)
    }

    LaunchedEffect(uiState.loadedCurrency) {
        uiState.loadedCurrency?.let {
            code = it.code
            name = it.name
            symbol = it.symbol
            rate = it.exchangeRateToDefault.stripTrailingZeros().toPlainString()
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onNavigateBack()
    }

    val defaultCode = uiState.defaultCurrency?.code ?: "USD"
    val isDefaultCurrency = uiState.loadedCurrency?.isDefault == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Currency" else "Add Currency") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 3) code = it.uppercase() },
                label = { Text("Code (e.g. GBP)") },
                enabled = !isEditMode,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = symbol,
                onValueChange = { symbol = it },
                label = { Text("Symbol") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text("Units per 1 $defaultCode") },
                enabled = !isDefaultCurrency,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    viewModel.saveCurrency(code, name, symbol, if (isDefaultCurrency) "1" else rate)
                },
                enabled = !uiState.isSuccess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "Save" else "Add Currency")
            }
        }
    }
}

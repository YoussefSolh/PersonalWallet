package com.youssefsolh.personalwallet.presentation.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.presentation.viewmodel.AddTransactionEnhancedViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreenEnhanced(
    walletId: String,
    transactionId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    viewModel: AddTransactionEnhancedViewModel = hiltViewModel()
) {
    val isEditMode = transactionId != null
    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedToWallet by remember { mutableStateOf<Wallet?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showWalletDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(walletId, selectedType) {
        viewModel.loadData(walletId, selectedType)
    }

    // Load transaction if editing
    LaunchedEffect(transactionId) {
        transactionId?.let { viewModel.loadTransaction(it) }
    }

    // Populate form with transaction data when loaded
    LaunchedEffect(uiState.transaction) {
        uiState.transaction?.let { transaction ->
            amount = transaction.amount.toString()
            description = transaction.description
            selectedType = transaction.type
            selectedDate = transaction.timestamp

            // For income and expense, set the category
            if (transaction.type != TransactionType.TRANSFER) {
                // Find and set the category from loaded categories
                selectedCategory = uiState.categories.find { it.id == transaction.categoryId }
            }
        }
    }

    // Separate effect for setting destination wallet (waits for otherWallets to load)
    LaunchedEffect(uiState.transaction, uiState.otherWallets) {
        uiState.transaction?.let { transaction ->
            if (transaction.type == TransactionType.TRANSFER && uiState.otherWallets.isNotEmpty()) {
                selectedToWallet = uiState.otherWallets.find { it.id == transaction.toWalletId }
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    // Category Selection Dialog
    if (showCategoryDialog && uiState.categories.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.categories) { category ->
                        CategoryItem(
                            category = category,
                            isSelected = selectedCategory?.id == category.id,
                            onClick = {
                                selectedCategory = category
                                showCategoryDialog = false
                            }
                        )
                    }
                    item {
                        TextButton(
                            onClick = {
                                showCategoryDialog = false
                                onNavigateToAddCategory()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add New Category")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Wallet Selection Dialog (for transfers)
    if (showWalletDialog && uiState.otherWallets.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showWalletDialog = false },
            title = { Text("Select Destination Wallet") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.otherWallets) { wallet ->
                        WalletItem(
                            wallet = wallet,
                            isSelected = selectedToWallet?.id == wallet.id,
                            onClick = {
                                selectedToWallet = wallet
                                showWalletDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWalletDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Transaction" else "Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Transaction Type Selector
                Text("Transaction Type", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = {
                            selectedType = TransactionType.INCOME
                            selectedCategory = null
                        },
                        label = { Text("Income") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = {
                            selectedType = TransactionType.EXPENSE
                            selectedCategory = null
                        },
                        label = { Text("Expense") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.TRANSFER,
                        onClick = {
                            selectedType = TransactionType.TRANSFER
                            selectedCategory = null
                        },
                        label = { Text("Transfer") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        amountError = null
                    },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    prefix = { Text("$") },
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } },
                    placeholder = { Text("0.00") }
                )
            }

            // Category Selection (only for INCOME and EXPENSE)
            if (selectedType != TransactionType.TRANSFER) {
                item {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedCategory != null) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = Color(android.graphics.Color.parseColor(selectedCategory!!.color))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = selectedCategory!!.icon,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(selectedCategory!!.name)
                            } else {
                                Text(
                                    "Select Category",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Destination Wallet Selection (only for TRANSFER)
            if (selectedType == TransactionType.TRANSFER) {
                item {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showWalletDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedToWallet != null) {
                                Column {
                                    Text(selectedToWallet!!.name)
                                    Text(
                                        "Balance: $${selectedToWallet!!.balance}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(
                                    "Select Destination Wallet",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        descriptionError = null
                    },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = descriptionError != null,
                    supportingText = descriptionError?.let { { Text(it) } },
                    placeholder = { Text("e.g., Grocery shopping") }
                )
            }

            // Date Selection
            item {
                Text("Date", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = dateFormatter.format(Date(selectedDate)),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            if (uiState.error != null) {
                item {
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
            }

            item {
                Button(
                    onClick = {
                        // Validate all fields
                        var hasError = false

                        if (amount.isBlank()) {
                            amountError = "Amount is required"
                            hasError = true
                        } else {
                            val amountValue = amount.toDoubleOrNull()
                            when {
                                amountValue == null -> {
                                    amountError = "Please enter a valid number"
                                    hasError = true
                                }
                                amountValue <= 0 -> {
                                    amountError = "Amount must be greater than zero"
                                    hasError = true
                                }
                            }
                        }

                        if (description.isBlank()) {
                            descriptionError = "Description is required"
                            hasError = true
                        } else if (description.length < 2) {
                            descriptionError = "Description must be at least 2 characters"
                            hasError = true
                        }

                        // Validate category for income/expense
                        if (selectedType != TransactionType.TRANSFER && selectedCategory == null) {
                            hasError = true
                            // Show snackbar or toast for category selection
                        }

                        // Validate destination wallet for transfers
                        if (selectedType == TransactionType.TRANSFER && selectedToWallet == null) {
                            hasError = true
                            // Show snackbar or toast for wallet selection
                        }

                        if (!hasError) {
                            if (isEditMode && transactionId != null) {
                                viewModel.updateTransaction(
                                    transactionId = transactionId,
                                    fromWalletId = walletId,
                                    toWalletId = selectedToWallet?.id,
                                    amount = amount.toDouble(),
                                    description = description.trim(),
                                    type = selectedType,
                                    categoryId = selectedCategory?.id ?: "default",
                                    timestamp = selectedDate
                                )
                            } else {
                                viewModel.addTransaction(
                                    fromWalletId = walletId,
                                    toWalletId = selectedToWallet?.id,
                                    amount = amount.toDouble(),
                                    description = description.trim(),
                                    type = selectedType,
                                    categoryId = selectedCategory?.id ?: "default",
                                    timestamp = selectedDate
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
                        Text(if (isEditMode) "Update Transaction" else "Add Transaction")
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = Color(android.graphics.Color.parseColor(category.color))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = category.icon,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(category.name)
        }
    }
}

@Composable
private fun WalletItem(
    wallet: Wallet,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(wallet.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    wallet.currency,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "$${wallet.balance}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

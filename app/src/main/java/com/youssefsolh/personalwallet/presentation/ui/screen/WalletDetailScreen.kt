package com.youssefsolh.personalwallet.presentation.ui.screen

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import com.youssefsolh.personalwallet.domain.model.TransactionType
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.youssefsolh.personalwallet.presentation.viewmodel.TransactionWithCategory
import com.youssefsolh.personalwallet.presentation.viewmodel.WalletDetailViewModel
import androidx.compose.ui.graphics.Color as ComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDetailScreen(
    walletId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddTransaction: (String) -> Unit,
    onNavigateToTransfer: (String) -> Unit = {},
    onNavigateToEditWallet: (String) -> Unit = {},
    onNavigateToEditTransaction: (String, String) -> Unit = { _, _ -> },
    viewModel: WalletDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTransactionWithCategory by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(walletId) {
        viewModel.loadWallet(walletId)
    }

    // Handle export success message
    LaunchedEffect(uiState.exportSuccess) {
        uiState.exportSuccess?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearExportSuccess()
        }
    }

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            if (message.startsWith("Export") || message.contains("transactions")) {
                snackbarHostState.showSnackbar(message)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.wallet?.name ?: "Wallet Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export")
                    }
                    IconButton(onClick = { onNavigateToEditWallet(walletId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Wallet")
                    }
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (showSearchBar) "Close Search" else "Search"
                        )
                    }
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { onNavigateToTransfer(walletId) }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Transfer")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddTransaction(walletId) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            uiState.wallet != null -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isLoading,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                    // Search Bar
                    if (showSearchBar) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            placeholder = { Text("Search transactions...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true
                        )
                    }

                    // Active Filter Chip
                    if (uiState.filterType != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.onFilterTypeChanged(null) },
                                label = { Text(uiState.filterType!!.name) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }

                    // Wallet Balance Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Current Balance",
                                fontSize = 16.sp
                            )
                            Text(
                                text = uiState.wallet?.getFormattedBalance() ?: "$0.00",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.wallet?.currency ?: "USD",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Transactions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (uiState.transactionsWithCategories.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "📝",
                                    fontSize = 64.sp
                                )
                                Text(
                                    text = "No Transactions Yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Start tracking your income and expenses by adding your first transaction",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Tap the + button below to add a transaction",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn {
                            items(uiState.transactionsWithCategories) { transactionWithCategory ->
                                TransactionItem(
                                    transactionWithCategory = transactionWithCategory,
                                    onClick = { selectedTransactionWithCategory = transactionWithCategory }
                                )
                            }
                        }
                    }

                    // Transaction Action Dialog
                    if (selectedTransactionWithCategory != null) {
                        AlertDialog(
                            onDismissRequest = { selectedTransactionWithCategory = null },
                            title = { Text("Transaction Actions") },
                            text = {
                                Column {
                                    Text(selectedTransactionWithCategory!!.transaction.description)
                                    Text(
                                        text = "${selectedTransactionWithCategory!!.transaction.type.name} - ${selectedTransactionWithCategory!!.displayAmount}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            confirmButton = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(
                                        onClick = {
                                            onNavigateToEditTransaction(
                                                walletId,
                                                selectedTransactionWithCategory!!.transaction.id
                                            )
                                            selectedTransactionWithCategory = null
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Edit")
                                    }
                                    TextButton(
                                        onClick = {
                                            showDeleteDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Delete")
                                    }
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { selectedTransactionWithCategory = null }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    // Delete Confirmation Dialog
                    if (showDeleteDialog && selectedTransactionWithCategory != null) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete Transaction") },
                            text = { Text("Are you sure you want to delete this transaction? This will update your wallet balance.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.deleteTransaction(selectedTransactionWithCategory!!.transaction)
                                        showDeleteDialog = false
                                        selectedTransactionWithCategory = null
                                    }
                                ) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showDeleteDialog = false
                                    }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    // Filter Dialog
                    if (showFilterDialog) {
                        AlertDialog(
                            onDismissRequest = { showFilterDialog = false },
                            title = { Text("Filter Transactions") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Select transaction type:")

                                    FilterChip(
                                        selected = uiState.filterType == null,
                                        onClick = {
                                            viewModel.onFilterTypeChanged(null)
                                            showFilterDialog = false
                                        },
                                        label = { Text("All") }
                                    )

                                    FilterChip(
                                        selected = uiState.filterType == TransactionType.INCOME,
                                        onClick = {
                                            viewModel.onFilterTypeChanged(TransactionType.INCOME)
                                            showFilterDialog = false
                                        },
                                        label = { Text("Income") }
                                    )

                                    FilterChip(
                                        selected = uiState.filterType == TransactionType.EXPENSE,
                                        onClick = {
                                            viewModel.onFilterTypeChanged(TransactionType.EXPENSE)
                                            showFilterDialog = false
                                        },
                                        label = { Text("Expense") }
                                    )

                                    FilterChip(
                                        selected = uiState.filterType == TransactionType.TRANSFER,
                                        onClick = {
                                            viewModel.onFilterTypeChanged(TransactionType.TRANSFER)
                                            showFilterDialog = false
                                        },
                                        label = { Text("Transfer") }
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showFilterDialog = false }) {
                                    Text("Close")
                                }
                            }
                        )
                    }

                    // Export Dialog
                    if (showExportDialog) {
                        AlertDialog(
                            onDismissRequest = { showExportDialog = false },
                            title = { Text("Export Transactions") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Choose export format:")
                                    Text(
                                        text = "Select the CSV format for your transaction export",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            confirmButton = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(
                                        onClick = {
                                            val intent = viewModel.exportToCsv(detailed = false)
                                            intent?.let { context.startActivity(Intent.createChooser(it, "Share CSV")) }
                                            showExportDialog = false
                                        }
                                    ) {
                                        Text("Basic")
                                    }
                                    TextButton(
                                        onClick = {
                                            val intent = viewModel.exportToCsv(detailed = true)
                                            intent?.let { context.startActivity(Intent.createChooser(it, "Share CSV")) }
                                            showExportDialog = false
                                        }
                                    ) {
                                        Text("Detailed")
                                    }
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showExportDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transactionWithCategory: TransactionWithCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = transactionWithCategory.category?.let { category ->
                        ComposeColor(android.graphics.Color.parseColor(category.color))
                    } ?: MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = transactionWithCategory.category?.icon ?: "💰",
                            fontSize = 24.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transactionWithCategory.transaction.description,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = transactionWithCategory.category?.name ?: transactionWithCategory.transaction.type.name,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = transactionWithCategory.displayAmount,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (transactionWithCategory.displayColor)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}

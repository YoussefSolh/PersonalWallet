# Personal Wallet - Enhancements & New Features Proposal
**Version**: 1.0
**Date**: November 26, 2025
**Based on**: Comprehensive Codebase Analysis

---

## Executive Summary

This document outlines proposed enhancements and new features for the Personal Wallet Android application based on a thorough analysis of the current codebase. Features are prioritized by user value, implementation complexity, and technical feasibility.

**Current Status**: 8.5/10 code quality, production-ready with recommended security improvements
**Total Proposed Features**: 25+ enhancements across 6 categories

---

## Table of Contents

1. [Critical Security & Stability Enhancements](#1-critical-security--stability-enhancements)
2. [High-Priority Feature Additions](#2-high-priority-feature-additions)
3. [User Experience Improvements](#3-user-experience-improvements)
4. [Advanced Analytics & Insights](#4-advanced-analytics--insights)
5. [Cloud & Synchronization Features](#5-cloud--synchronization-features)
6. [Nice-to-Have Features](#6-nice-to-have-features)
7. [Implementation Roadmap](#7-implementation-roadmap)

---

## 1. Critical Security & Stability Enhancements

### 1.1 Database Encryption (CRITICAL)
**Priority**: P0 - Must Have
**Estimated Time**: 4-6 hours
**User Impact**: High - Protects sensitive financial data

**Current Issue**: Room database stores financial data in plain text on device storage.

**Proposed Solution**: Implement SQLCipher for database encryption

**Implementation Steps**:
```kotlin
// 1. Add dependency to build.gradle.kts
implementation("net.zetetic:android-database-sqlcipher:4.5.4")
implementation("androidx.sqlite:sqlite-ktx:2.4.0")

// 2. Modify DatabaseModule.kt
@Provides
@Singleton
fun provideWalletDatabase(@ApplicationContext context: Context): WalletDatabase {
    val passphrase = getSavedOrGeneratePassphrase(context)
    val factory = SupportFactory(passphrase)

    return Room.databaseBuilder(
        context,
        WalletDatabase::class.java,
        "wallet_database"
    )
    .openHelperFactory(factory)
    .build()
}

private fun getSavedOrGeneratePassphrase(context: Context): ByteArray {
    // Use EncryptedSharedPreferences to store database passphrase
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "encrypted_db_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    return encryptedPrefs.getString("db_passphrase", null)?.toByteArray()
        ?: generateSecurePassphrase().also { passphrase ->
            encryptedPrefs.edit().putString("db_passphrase", String(passphrase)).apply()
        }
}

private fun generateSecurePassphrase(): ByteArray {
    return ByteArray(32).apply {
        SecureRandom().nextBytes(this)
    }
}
```

**Benefits**:
- Data encrypted at rest (AES-256)
- Passphrase stored in Android Keystore
- Compliance with financial data protection standards
- No API changes required

**Testing**:
- Verify database migration from unencrypted to encrypted
- Test backup/restore with encrypted database
- Performance testing (encryption overhead typically <5%)

---

### 1.2 Database Migration System (HIGH)
**Priority**: P0 - Must Have
**Estimated Time**: 3-4 hours
**User Impact**: High - Required for app updates

**Current Issue**: Empty migration defined but not implemented in WalletDatabase.kt lines 78-81

**Proposed Solution**: Implement proper schema versioning and migrations

**Implementation**:
```kotlin
// WalletDatabase.kt
@Database(
    entities = [WalletEntity::class, TransactionEntity::class, CategoryEntity::class],
    version = 2,  // Bump version
    exportSchema = true  // Enable schema export for version control
)
abstract class WalletDatabase : RoomDatabase() {

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Add new column for future feature
                database.execSQL(
                    "ALTER TABLE wallets ADD COLUMN icon TEXT DEFAULT '💰'"
                )
            }
        }

        // Future migrations
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add recurring_transactions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS recurring_transactions (
                        id TEXT PRIMARY KEY NOT NULL,
                        amount TEXT NOT NULL,
                        description TEXT NOT NULL,
                        frequency TEXT NOT NULL,
                        next_date INTEGER NOT NULL,
                        category_id TEXT NOT NULL,
                        wallet_id TEXT NOT NULL,
                        is_active INTEGER NOT NULL DEFAULT 1
                    )
                """)
            }
        }
    }
}

// DatabaseModule.kt
@Provides
@Singleton
fun provideWalletDatabase(@ApplicationContext context: Context): WalletDatabase {
    return Room.databaseBuilder(context, WalletDatabase::class.java, "wallet_database")
        .addMigrations(
            WalletDatabase.MIGRATION_1_2,
            WalletDatabase.MIGRATION_2_3
        )
        .fallbackToDestructiveMigration()  // Only for development
        .build()
}
```

**Schema Export Configuration**:
```kotlin
// build.gradle.kts
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += "room.schemaLocation" to "$projectDir/schemas"
            }
        }
    }
}

kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}
```

**Benefits**:
- Smooth app updates without data loss
- Version-controlled database schemas
- Easier debugging of migration issues
- Production-ready update mechanism

---

### 1.3 ProGuard/R8 Configuration (HIGH)
**Priority**: P1 - Should Have
**Estimated Time**: 2-3 hours
**User Impact**: Medium - Smaller APK size, better security

**Current Issue**: `isMinifyEnabled = false` in release builds

**Proposed Solution**: Enable code shrinking with proper keep rules

**Implementation**:
```kotlin
// app/build.gradle.kts
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

**ProGuard Rules** (`proguard-rules.pro`):
```proguard
# Keep Room entities
-keep class com.youssefsolh.personalwallet.data.local.entity.** { *; }

# Keep domain models for serialization
-keep class com.youssefsolh.personalwallet.domain.model.** { *; }

# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
```

**Benefits**:
- APK size reduction (typically 30-50%)
- Obfuscated code (harder to reverse-engineer)
- Faster app performance
- Better security for release builds

---

### 1.4 Comprehensive Error Handling (MEDIUM)
**Priority**: P1 - Should Have
**Estimated Time**: 4-5 hours
**User Impact**: High - Better user experience during errors

**Current Issue**: Limited global error handling, some hardcoded error messages

**Proposed Solution**: Implement global error handler with user-friendly messages

**Implementation**:
```kotlin
// New file: presentation/error/ErrorHandler.kt
sealed class AppError {
    data class NetworkError(val message: String) : AppError()
    data class DatabaseError(val message: String) : AppError()
    data class AuthenticationError(val message: String) : AppError()
    data class ValidationError(val field: String, val message: String) : AppError()
    data class BackupError(val message: String) : AppError()
    data class UnknownError(val throwable: Throwable) : AppError()
}

object ErrorHandler {
    fun handleError(error: Throwable): AppError {
        return when (error) {
            is IOException -> AppError.NetworkError("Unable to connect. Please check your internet connection.")
            is SQLException -> AppError.DatabaseError("Database error occurred. Please try again.")
            is GoogleAuthException -> AppError.AuthenticationError("Authentication failed. Please sign in again.")
            is IllegalArgumentException -> AppError.ValidationError("input", error.message ?: "Invalid input")
            else -> AppError.UnknownError(error)
        }
    }

    fun getErrorMessage(error: AppError): String {
        return when (error) {
            is AppError.NetworkError -> error.message
            is AppError.DatabaseError -> error.message
            is AppError.AuthenticationError -> error.message
            is AppError.ValidationError -> "${error.field}: ${error.message}"
            is AppError.BackupError -> error.message
            is AppError.UnknownError -> "An unexpected error occurred. Please try again."
        }
    }
}

// MainActivity.kt - Global error handling
class MainActivity : ComponentActivity() {
    private val errorChannel = Channel<AppError>(Channel.BUFFERED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Global exception handler
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            lifecycleScope.launch {
                errorChannel.send(ErrorHandler.handleError(throwable))
            }
        }

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                errorChannel.receiveAsFlow().collect { error ->
                    snackbarHostState.showSnackbar(
                        message = ErrorHandler.getErrorMessage(error),
                        duration = SnackbarDuration.Long
                    )
                }
            }

            PersonalWalletTheme {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    // App content
                }
            }
        }
    }
}
```

**ViewModel Integration**:
```kotlin
// Base ViewModel for error handling
abstract class BaseViewModel : ViewModel() {
    private val _error = MutableStateFlow<AppError?>(null)
    val error: StateFlow<AppError?> = _error.asStateFlow()

    protected fun handleError(throwable: Throwable) {
        _error.value = ErrorHandler.handleError(throwable)
    }

    fun clearError() {
        _error.value = null
    }
}

// Usage in ViewModels
class WalletDetailViewModel @Inject constructor(
    // ...
) : BaseViewModel() {

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                deleteTransactionUseCase(transaction)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
}
```

**Benefits**:
- Consistent error messaging across app
- Better debugging with error categorization
- Improved user experience
- Centralized error logging

---

### 1.5 Retry Logic for Drive API (MEDIUM)
**Priority**: P2 - Nice to Have
**Estimated Time**: 3-4 hours
**User Impact**: Medium - More reliable cloud backups

**Current Issue**: No retry logic for transient network failures in DriveService.kt

**Proposed Solution**: Implement exponential backoff retry mechanism

**Implementation**:
```kotlin
// New file: data/remote/RetryPolicy.kt
class RetryPolicy(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000,
    private val maxDelayMs: Long = 10000,
    private val factor: Double = 2.0
) {
    suspend fun <T> retry(block: suspend () -> T): T {
        var currentDelay = initialDelayMs
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: IOException) {
                if (attempt == maxRetries - 1) throw e
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
            }
        }
        throw IOException("Max retries exceeded")
    }
}

// DriveService.kt - Modified methods
class DriveService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val retryPolicy = RetryPolicy()

    suspend fun uploadBackup(backupData: BackupData, userEmail: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                retryPolicy.retry {
                    val driveService = getDriveService(userEmail)
                        ?: return@retry Result.failure(Exception("Failed to initialize Drive service"))

                    // Upload logic...
                }
                Result.success("Backup uploaded successfully")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

**Benefits**:
- More reliable cloud operations
- Better handling of temporary network issues
- Improved user experience
- Configurable retry parameters

---

## 2. High-Priority Feature Additions

### 2.1 Recurring Transactions (P0)
**Priority**: P0 - Must Have
**Estimated Time**: 8-10 hours
**User Impact**: Very High - Highly requested feature

**Description**: Automatic creation of recurring transactions (salary, rent, subscriptions)

**Database Schema**:
```kotlin
// New entity: RecurringTransactionEntity.kt
@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey val id: String,
    val amount: String,
    val description: String,
    val categoryId: String,
    val walletId: String,
    val type: String,  // INCOME, EXPENSE
    val frequency: String,  // DAILY, WEEKLY, MONTHLY, YEARLY
    val startDate: Long,
    val nextOccurrence: Long,
    val endDate: Long?,  // Nullable for indefinite
    val dayOfMonth: Int?,  // For monthly: 1-31
    val dayOfWeek: Int?,  // For weekly: 1-7
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)

// Domain model
data class RecurringTransaction(
    val id: String,
    val amount: BigDecimal,
    val description: String,
    val categoryId: String,
    val walletId: String,
    val type: TransactionType,
    val frequency: RecurrenceFrequency,
    val startDate: Long,
    val nextOccurrence: Long,
    val endDate: Long?,
    val dayOfMonth: Int?,
    val dayOfWeek: Int?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

enum class RecurrenceFrequency {
    DAILY, WEEKLY, MONTHLY, YEARLY
}
```

**Use Cases**:
```kotlin
// CreateRecurringTransactionUseCase.kt
class CreateRecurringTransactionUseCase @Inject constructor(
    private val repository: RecurringTransactionRepository
) {
    suspend operator fun invoke(recurringTransaction: RecurringTransaction): Result<Unit> {
        return try {
            repository.insertRecurringTransaction(recurringTransaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ProcessRecurringTransactionsUseCase.kt
class ProcessRecurringTransactionsUseCase @Inject constructor(
    private val recurringRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return try {
            val now = System.currentTimeMillis()
            val dueTransactions = recurringRepository.getDueRecurringTransactions(now)

            var processedCount = 0
            dueTransactions.forEach { recurring ->
                // Create actual transaction
                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = recurring.amount,
                    type = recurring.type,
                    description = "${recurring.description} (Auto)",
                    categoryId = recurring.categoryId,
                    fromWalletId = if (recurring.type == TransactionType.EXPENSE) recurring.walletId else null,
                    toWalletId = if (recurring.type == TransactionType.INCOME) recurring.walletId else null,
                    timestamp = now,
                    createdAt = now,
                    updatedAt = now
                )

                transactionRepository.insertTransaction(transaction)

                // Update wallet balance
                val wallet = walletRepository.getWalletById(recurring.walletId)
                wallet?.let {
                    val newBalance = when (recurring.type) {
                        TransactionType.INCOME -> it.balance + recurring.amount
                        TransactionType.EXPENSE -> it.balance - recurring.amount
                        else -> it.balance
                    }
                    walletRepository.updateWallet(it.copy(balance = newBalance))
                }

                // Calculate next occurrence
                val nextOccurrence = calculateNextOccurrence(recurring)
                recurringRepository.updateNextOccurrence(recurring.id, nextOccurrence)

                processedCount++
            }

            Result.success(processedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateNextOccurrence(recurring: RecurringTransaction): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = recurring.nextOccurrence
        }

        when (recurring.frequency) {
            RecurrenceFrequency.DAILY -> calendar.add(Calendar.DAY_OF_MONTH, 1)
            RecurrenceFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RecurrenceFrequency.MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)
                recurring.dayOfMonth?.let { calendar.set(Calendar.DAY_OF_MONTH, it) }
            }
            RecurrenceFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
        }

        return calendar.timeInMillis
    }
}
```

**Background Processing**:
```kotlin
// RecurringTransactionWorker.kt
class RecurringTransactionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val processUseCase = // Get from DI

        return when (val result = processUseCase()) {
            is kotlin.Result.Success -> Result.success()
            is kotlin.Result.Failure -> Result.retry()
        }
    }
}

// Schedule in MainActivity or Application
class WalletApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleRecurringTransactionWorker()
    }

    private fun scheduleRecurringTransactionWorker() {
        val workRequest = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
            1, TimeUnit.DAYS  // Check daily
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recurring_transactions",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
```

**UI Implementation**:
```kotlin
// RecurringTransactionsScreen.kt
@Composable
fun RecurringTransactionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: RecurringTransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Transactions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, "Add Recurring Transaction")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(uiState.recurringTransactions) { recurring ->
                RecurringTransactionCard(
                    recurring = recurring,
                    onClick = { /* Navigate to edit */ },
                    onToggle = { viewModel.toggleActive(recurring.id) }
                )
            }
        }
    }
}
```

**Benefits**:
- Automatic transaction creation
- Never miss salary or bill payments
- Reduces manual data entry
- Better financial planning

---

### 2.2 Budget Management (P0)
**Priority**: P0 - Must Have
**Estimated Time**: 10-12 hours
**User Impact**: Very High - Essential for financial planning

**Description**: Set spending limits per category with alerts

**Database Schema**:
```kotlin
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val amount: String,  // Budget limit
    val period: String,  // WEEKLY, MONTHLY, YEARLY
    val startDate: Long,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)

// Domain model
data class Budget(
    val id: String,
    val categoryId: String,
    val amount: BigDecimal,
    val period: BudgetPeriod,
    val startDate: Long,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

enum class BudgetPeriod {
    WEEKLY, MONTHLY, YEARLY
}

// Extended model for UI
data class BudgetWithProgress(
    val budget: Budget,
    val category: Category,
    val spent: BigDecimal,
    val remaining: BigDecimal,
    val percentage: Float,
    val isOverBudget: Boolean
)
```

**Use Cases**:
```kotlin
// GetBudgetProgressUseCase.kt
class GetBudgetProgressUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<BudgetWithProgress>> {
        return budgetRepository.getAllBudgets()
            .map { budgets ->
                budgets.map { budget ->
                    val category = categoryRepository.getCategoryById(budget.categoryId)
                    val (startDate, endDate) = calculatePeriodDates(budget.period, budget.startDate)

                    val spent = transactionRepository.getSpentInCategoryBetween(
                        categoryId = budget.categoryId,
                        startDate = startDate,
                        endDate = endDate
                    )

                    val remaining = budget.amount - spent
                    val percentage = (spent / budget.amount).toFloat()

                    BudgetWithProgress(
                        budget = budget,
                        category = category,
                        spent = spent,
                        remaining = remaining,
                        percentage = percentage,
                        isOverBudget = spent > budget.amount
                    )
                }
            }
    }

    private fun calculatePeriodDates(period: BudgetPeriod, startDate: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply { timeInMillis = startDate }

        val endCalendar = calendar.clone() as Calendar
        when (period) {
            BudgetPeriod.WEEKLY -> endCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            BudgetPeriod.MONTHLY -> endCalendar.add(Calendar.MONTH, 1)
            BudgetPeriod.YEARLY -> endCalendar.add(Calendar.YEAR, 1)
        }

        return calendar.timeInMillis to endCalendar.timeInMillis
    }
}
```

**UI Implementation**:
```kotlin
@Composable
fun BudgetCard(budget: BudgetWithProgress) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = budget.category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${budget.spent} / $${budget.budget.amount}",
                    color = if (budget.isOverBudget)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = budget.percentage.coerceAtMost(1.0f),
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    budget.isOverBudget -> MaterialTheme.colorScheme.error
                    budget.percentage > 0.8f -> Color.Yellow
                    else -> MaterialTheme.colorScheme.primary
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (budget.isOverBudget)
                    "Over budget by $${budget.spent - budget.budget.amount}"
                else
                    "$${budget.remaining} remaining",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

**Notification System**:
```kotlin
// BudgetAlertWorker.kt
class BudgetAlertWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val getBudgetProgressUseCase = // Get from DI

        getBudgetProgressUseCase().collect { budgets ->
            budgets.forEach { budget ->
                when {
                    budget.percentage >= 1.0f -> {
                        showNotification(
                            "Budget Exceeded!",
                            "You've exceeded your ${budget.category.name} budget"
                        )
                    }
                    budget.percentage >= 0.8f -> {
                        showNotification(
                            "Budget Warning",
                            "You've used 80% of your ${budget.category.name} budget"
                        )
                    }
                }
            }
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(applicationContext, "budget_alerts")
            .setSmallIcon(R.drawable.ic_wallet)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}
```

**Benefits**:
- Better spending control
- Proactive budget warnings
- Visual progress tracking
- Prevents overspending

---

### 2.3 Bill Reminders (P1)
**Priority**: P1 - Should Have
**Estimated Time**: 6-8 hours
**User Impact**: High - Prevents missed payments

**Description**: Set reminders for upcoming bills with notifications

**Implementation**:
```kotlin
// Extension to RecurringTransaction
data class BillReminder(
    val recurringTransactionId: String,
    val reminderDaysBefore: Int,  // Remind X days before due date
    val isEnabled: Boolean
)

// AlarmManager integration
class BillReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleReminder(recurring: RecurringTransaction, daysBefore: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerTime = recurring.nextOccurrence - (daysBefore * 24 * 60 * 60 * 1000)

        val intent = Intent(context, BillReminderReceiver::class.java).apply {
            putExtra("TRANSACTION_ID", recurring.id)
            putExtra("DESCRIPTION", recurring.description)
            putExtra("AMOUNT", recurring.amount.toString())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            recurring.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}

// BroadcastReceiver for reminders
class BillReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val description = intent.getStringExtra("DESCRIPTION") ?: ""
        val amount = intent.getStringExtra("AMOUNT") ?: ""

        val notification = NotificationCompat.Builder(context, "bill_reminders")
            .setSmallIcon(R.drawable.ic_reminder)
            .setContentTitle("Bill Reminder")
            .setContentText("$description - $amount is due soon")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(intent.getIntExtra("TRANSACTION_ID", 0), notification)
    }
}
```

**Benefits**:
- Never miss bill payments
- Avoid late fees
- Better cash flow management
- Peace of mind

---

### 2.4 Multi-Currency Exchange Rates (P1)
**Priority**: P1 - Should Have
**Estimated Time**: 8-10 hours
**User Impact**: High - Essential for international users

**Description**: Support multiple currencies with real-time exchange rates

**Database Schema**:
```kotlin
@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey val currencyPair: String,  // e.g., "USD_EUR"
    val rate: String,
    val lastUpdated: Long
)

// API model
data class ExchangeRateResponse(
    val base: String,
    val rates: Map<String, Double>,
    val timestamp: Long
)
```

**API Integration**:
```kotlin
// ExchangeRateService.kt
interface ExchangeRateApi {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("base") baseCurrency: String = "USD"
    ): ExchangeRateResponse
}

class ExchangeRateService @Inject constructor(
    private val api: ExchangeRateApi,
    private val dao: ExchangeRateDao
) {
    suspend fun updateRates(baseCurrency: String = "USD"): Result<Unit> {
        return try {
            val response = api.getLatestRates(baseCurrency)

            response.rates.forEach { (currency, rate) ->
                val entity = ExchangeRateEntity(
                    currencyPair = "${baseCurrency}_$currency",
                    rate = rate.toString(),
                    lastUpdated = response.timestamp
                )
                dao.insertRate(entity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun convert(amount: BigDecimal, from: String, to: String): BigDecimal {
        if (from == to) return amount

        val rate = dao.getRate("${from}_$to")
            ?: throw IllegalStateException("Exchange rate not found")

        return amount * BigDecimal(rate.rate)
    }
}
```

**Dependency Addition**:
```kotlin
// build.gradle.kts
dependencies {
    // Exchange rate API (use free tier: exchangerate-api.com or fixer.io)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}
```

**UI Integration**:
```kotlin
// WalletCard with currency conversion
@Composable
fun WalletCardWithCurrency(
    wallet: Wallet,
    displayCurrency: String,
    exchangeRate: BigDecimal?
) {
    Card {
        Column {
            Text(wallet.name)
            Text("${wallet.currency} ${wallet.balance}")

            if (wallet.currency != displayCurrency && exchangeRate != null) {
                val converted = wallet.balance * exchangeRate
                Text(
                    "≈ $displayCurrency $converted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

**Benefits**:
- Support for international users
- Accurate multi-currency tracking
- Real-time exchange rates
- Better financial overview

---

## 3. User Experience Improvements

### 3.1 Transaction Search Enhancement (P2)
**Priority**: P2 - Nice to Have
**Estimated Time**: 4-5 hours
**User Impact**: Medium - Improved usability

**Current Issue**: Basic search only in WalletDetailScreen

**Proposed Solution**: Advanced search with filters and sorting

**Implementation**:
```kotlin
// SearchFilter.kt
data class TransactionSearchFilter(
    val query: String = "",
    val types: Set<TransactionType> = emptySet(),
    val categories: Set<String> = emptySet(),
    val wallets: Set<String> = emptySet(),
    val minAmount: BigDecimal? = null,
    val maxAmount: BigDecimal? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val sortBy: SortOption = SortOption.DATE_DESC
)

enum class SortOption {
    DATE_ASC, DATE_DESC, AMOUNT_ASC, AMOUNT_DESC, DESCRIPTION
}

// Advanced TransactionDao query
@Query("""
    SELECT * FROM transactions
    WHERE (:query IS NULL OR description LIKE '%' || :query || '%')
    AND (:types IS NULL OR type IN (:types))
    AND (:categories IS NULL OR categoryId IN (:categories))
    AND (:wallets IS NULL OR fromWalletId IN (:wallets) OR toWalletId IN (:wallets))
    AND (:minAmount IS NULL OR CAST(amount AS REAL) >= :minAmount)
    AND (:maxAmount IS NULL OR CAST(amount AS REAL) <= :maxAmount)
    AND (:startDate IS NULL OR timestamp >= :startDate)
    AND (:endDate IS NULL OR timestamp <= :endDate)
    AND isDeleted = 0
    ORDER BY
        CASE WHEN :sortBy = 'DATE_DESC' THEN timestamp END DESC,
        CASE WHEN :sortBy = 'DATE_ASC' THEN timestamp END ASC,
        CASE WHEN :sortBy = 'AMOUNT_DESC' THEN CAST(amount AS REAL) END DESC,
        CASE WHEN :sortBy = 'AMOUNT_ASC' THEN CAST(amount AS REAL) END ASC,
        CASE WHEN :sortBy = 'DESCRIPTION' THEN description END ASC
""")
fun searchTransactions(
    query: String?,
    types: List<String>?,
    categories: List<String>?,
    wallets: List<String>?,
    minAmount: Double?,
    maxAmount: Double?,
    startDate: Long?,
    endDate: Long?,
    sortBy: String
): Flow<List<TransactionEntity>>
```

**UI - Search Screen**:
```kotlin
@Composable
fun AdvancedSearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    var showFilters by remember { mutableStateOf(false) }
    val filter by viewModel.currentFilter.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SearchBar(
                query = filter.query,
                onQueryChange = { viewModel.updateQuery(it) },
                onFilterClick = { showFilters = true }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(results) { transaction ->
                TransactionSearchResultCard(transaction)
            }
        }
    }

    if (showFilters) {
        FilterBottomSheet(
            filter = filter,
            onApply = { viewModel.applyFilter(it) },
            onDismiss = { showFilters = false }
        )
    }
}
```

**Benefits**:
- Find transactions quickly
- Complex filtering options
- Better data analysis
- Improved user productivity

---

### 3.2 Data Visualization & Charts (P1)
**Priority**: P1 - Should Have
**Estimated Time**: 10-12 hours
**User Impact**: High - Better insights

**Description**: Add charts for spending trends, category breakdowns, income vs expenses

**Library Selection**:
```kotlin
// build.gradle.kts
dependencies {
    // Vico Charts - Material Design 3 compatible
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    implementation("com.patrykandpatrick.vico:core:1.13.1")
}
```

**Implementation Examples**:

**1. Pie Chart - Spending by Category**:
```kotlin
@Composable
fun SpendingByCategoryChart(data: List<CategorySpending>) {
    val chartEntryModel = entryModelOf(
        data.mapIndexed { index, spending ->
            entryOf(index.toFloat(), spending.amount.toFloat())
        }
    )

    Chart(
        chart = pieChart(
            slices = data.map { spending ->
                PieChart.Slice(
                    color = Color(android.graphics.Color.parseColor(spending.category.color)),
                    label = spending.category.name
                )
            }
        ),
        model = chartEntryModel,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}
```

**2. Line Chart - Balance Over Time**:
```kotlin
@Composable
fun BalanceOverTimeChart(balanceHistory: List<BalanceSnapshot>) {
    val chartEntryModel = entryModelOf(
        balanceHistory.mapIndexed { index, snapshot ->
            entryOf(index.toFloat(), snapshot.balance.toFloat())
        }
    )

    Chart(
        chart = lineChart(
            lines = listOf(
                LineChart.Line(
                    lineColor = MaterialTheme.colorScheme.primary,
                    lineBackgroundShader = DynamicShaders.fromBrush(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                            )
                        )
                    )
                )
            )
        ),
        model = chartEntryModel,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { value, _ ->
                balanceHistory.getOrNull(value.toInt())?.date?.let {
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it))
                } ?: ""
            }
        )
    )
}

data class BalanceSnapshot(
    val date: Long,
    val balance: BigDecimal
)
```

**3. Bar Chart - Monthly Income vs Expenses**:
```kotlin
@Composable
fun IncomeVsExpenseChart(monthlyData: List<MonthlyData>) {
    val chartEntryModel = entryModelOf(
        monthlyData.mapIndexed { index, data ->
            entryOf(index.toFloat(), data.income.toFloat())
        },
        monthlyData.mapIndexed { index, data ->
            entryOf(index.toFloat(), data.expense.toFloat())
        }
    )

    Chart(
        chart = columnChart(
            columns = listOf(
                LineComponent(
                    color = Color.Green.toArgb(),
                    thicknessDp = 16f
                ),
                LineComponent(
                    color = Color.Red.toArgb(),
                    thicknessDp = 16f
                )
            )
        ),
        model = chartEntryModel,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis()
    )
}

data class MonthlyData(
    val month: String,
    val income: BigDecimal,
    val expense: BigDecimal
)
```

**Enhanced Reports Screen**:
```kotlin
@Composable
fun EnhancedReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn {
        item {
            // Summary Cards
            SummaryCards(
                totalIncome = uiState.totalIncome,
                totalExpense = uiState.totalExpense,
                netIncome = uiState.netIncome
            )
        }

        item {
            Text("Income vs Expenses", style = MaterialTheme.typography.titleLarge)
            IncomeVsExpenseChart(uiState.monthlyData)
        }

        item {
            Text("Spending by Category", style = MaterialTheme.typography.titleLarge)
            SpendingByCategoryChart(uiState.categorySpending)
        }

        item {
            Text("Balance Trend", style = MaterialTheme.typography.titleLarge)
            BalanceOverTimeChart(uiState.balanceHistory)
        }
    }
}
```

**Benefits**:
- Visual data insights
- Easier trend identification
- Better financial understanding
- More engaging user experience

---

### 3.3 Onboarding Flow (P2)
**Priority**: P2 - Nice to Have
**Estimated Time**: 6-8 hours
**User Impact**: Medium - Better first-time experience

**Description**: Guide new users through app features

**Implementation**:
```kotlin
// OnboardingScreen.kt
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    title = "Welcome to Personal Wallet",
                    description = "Manage your finances with ease",
                    icon = Icons.Default.AccountBalance
                )
                1 -> OnboardingPage(
                    title = "Track Your Spending",
                    description = "Categorize transactions and see where your money goes",
                    icon = Icons.Default.PieChart
                )
                2 -> OnboardingPage(
                    title = "Set Budgets",
                    description = "Stay on track with budget alerts",
                    icon = Icons.Default.Notifications
                )
                3 -> OnboardingPage(
                    title = "Sync Your Data",
                    description = "Backup to Google Drive for safety",
                    icon = Icons.Default.Cloud
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (pagerState.currentPage > 0) {
                TextButton(onClick = {
                    // Go back
                }) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            if (pagerState.currentPage < 3) {
                Button(onClick = {
                    // Next page
                }) {
                    Text("Next")
                }
            } else {
                Button(onClick = onComplete) {
                    Text("Get Started")
                }
            }
        }
    }
}
```

**Benefits**:
- Better user onboarding
- Feature discovery
- Reduced confusion
- Higher user retention

---

### 3.4 Quick Actions & Shortcuts (P2)
**Priority**: P2 - Nice to Have
**Estimated Time**: 3-4 hours
**User Impact**: Medium - Improved efficiency

**Description**: Add app shortcuts and quick actions

**Implementation**:
```kotlin
// MainActivity.kt
private fun createDynamicShortcuts() {
    val shortcutManager = getSystemService(ShortcutManager::class.java)

    val addExpense = ShortcutInfo.Builder(this, "add_expense")
        .setShortLabel("Add Expense")
        .setLongLabel("Add New Expense")
        .setIcon(Icon.createWithResource(this, R.drawable.ic_expense))
        .setIntent(
            Intent(Intent.ACTION_VIEW, Uri.parse("personalwallet://add_transaction?type=expense"))
        )
        .build()

    val addIncome = ShortcutInfo.Builder(this, "add_income")
        .setShortLabel("Add Income")
        .setLongLabel("Add New Income")
        .setIcon(Icon.createWithResource(this, R.drawable.ic_income))
        .setIntent(
            Intent(Intent.ACTION_VIEW, Uri.parse("personalwallet://add_transaction?type=income"))
        )
        .build()

    shortcutManager.dynamicShortcuts = listOf(addExpense, addIncome)
}

// shortcuts.xml (res/xml/)
<shortcuts xmlns:android="http://schemas.android.com/apk/res/android">
    <shortcut
        android:shortcutId="add_expense"
        android:enabled="true"
        android:icon="@drawable/ic_expense"
        android:shortcutShortLabel="@string/add_expense"
        android:shortcutLongLabel="@string/add_expense_long">
        <intent
            android:action="android.intent.action.VIEW"
            android:targetPackage="com.youssefsolh.personalwallet"
            android:targetClass="com.youssefsolh.personalwallet.MainActivity"
            android:data="personalwallet://add_transaction?type=expense" />
    </shortcut>
</shortcuts>
```

**Benefits**:
- Faster transaction entry
- Better user productivity
- Native Android integration
- Improved UX

---

## 4. Advanced Analytics & Insights

### 4.1 Spending Trends & Forecasting (P1)
**Priority**: P1 - Should Have
**Estimated Time**: 8-10 hours
**User Impact**: High - Valuable insights

**Description**: Analyze spending patterns and predict future expenses

**Implementation**:
```kotlin
// TrendAnalysisUseCase.kt
class TrendAnalysisUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        categoryId: String,
        months: Int = 6
    ): SpendingTrend {
        val startDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, -months)
        }.timeInMillis

        val transactions = transactionRepository.getTransactionsByCategoryBetween(
            categoryId,
            startDate,
            System.currentTimeMillis()
        )

        // Group by month
        val monthlySpending = transactions
            .groupBy {
                Calendar.getInstance().apply { timeInMillis = it.timestamp }
                    .get(Calendar.MONTH)
            }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        // Calculate trend
        val average = monthlySpending.values.average()
        val trend = calculateTrend(monthlySpending.values.toList())

        // Forecast next month
        val forecast = average + (trend * monthlySpending.size)

        return SpendingTrend(
            average = average.toBigDecimal(),
            trend = trend,
            forecast = forecast.toBigDecimal(),
            monthlyData = monthlySpending
        )
    }

    private fun calculateTrend(values: List<BigDecimal>): Double {
        // Simple linear regression
        val n = values.size
        val xSum = (1..n).sum()
        val ySum = values.sumOf { it.toDouble() }
        val xySum = values.mapIndexed { index, value ->
            (index + 1) * value.toDouble()
        }.sum()
        val x2Sum = (1..n).sumOf { it * it }

        return (n * xySum - xSum * ySum) / (n * x2Sum - xSum * xSum)
    }
}

data class SpendingTrend(
    val average: BigDecimal,
    val trend: Double,  // Positive = increasing, Negative = decreasing
    val forecast: BigDecimal,
    val monthlyData: Map<Int, BigDecimal>
)
```

**UI Implementation**:
```kotlin
@Composable
fun TrendInsightCard(trend: SpendingTrend, categoryName: String) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$categoryName Trend", style = MaterialTheme.typography.titleMedium)

            Row {
                Icon(
                    imageVector = if (trend.trend > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (trend.trend > 0) Color.Red else Color.Green
                )
                Text(
                    if (trend.trend > 0) "Spending increasing" else "Spending decreasing"
                )
            }

            Text("Average: $${trend.average}")
            Text("Forecast next month: $${trend.forecast}")

            // Trend line chart
            TrendLineChart(trend.monthlyData)
        }
    }
}
```

**Benefits**:
- Predict future expenses
- Identify spending patterns
- Better financial planning
- Actionable insights

---

### 4.2 Smart Categorization (P2)
**Priority**: P2 - Nice to Have
**Estimated Time**: 12-15 hours
**User Impact**: Medium - Reduced manual work

**Description**: Suggest categories based on transaction description using ML

**Implementation** (Basic rule-based, can be enhanced with ML later):
```kotlin
// SmartCategorizationUseCase.kt
class SmartCategorizationUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    private val categoryKeywords = mapOf(
        "Food & Dining" to listOf("restaurant", "food", "lunch", "dinner", "cafe", "pizza", "burger"),
        "Transportation" to listOf("uber", "taxi", "gas", "fuel", "parking", "metro", "bus"),
        "Shopping" to listOf("amazon", "walmart", "store", "mall", "shop"),
        "Entertainment" to listOf("movie", "cinema", "netflix", "spotify", "game"),
        "Utilities" to listOf("electric", "water", "internet", "phone", "bill"),
        "Healthcare" to listOf("doctor", "pharmacy", "hospital", "medicine", "health")
    )

    suspend fun suggestCategory(description: String): Category? {
        val lowercaseDesc = description.lowercase()

        // Find matching category
        val matchedCategory = categoryKeywords.entries
            .firstOrNull { (_, keywords) ->
                keywords.any { keyword -> lowercaseDesc.contains(keyword) }
            }?.key

        return matchedCategory?.let { categoryName ->
            categoryRepository.getCategoriesByType(TransactionType.EXPENSE)
                .firstOrNull { it.name == categoryName }
        }
    }
}

// Enhanced with ML (TensorFlow Lite)
class MLSmartCategorizationUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val categoryRepository: CategoryRepository
) {
    private val interpreter: Interpreter by lazy {
        val model = FileUtil.loadMappedFile(context, "category_classifier.tflite")
        Interpreter(model)
    }

    suspend fun suggestCategory(description: String): Category? {
        // Tokenize and vectorize description
        val input = vectorizeDescription(description)
        val output = Array(1) { FloatArray(categoryRepository.getAllCategories().size) }

        interpreter.run(input, output)

        // Get category with highest probability
        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: return null
        val categories = categoryRepository.getAllCategories()

        return categories.getOrNull(maxIndex)
    }
}
```

**Benefits**:
- Faster transaction entry
- Consistent categorization
- Learns from user behavior
- Reduces errors

---

## 5. Cloud & Synchronization Features

### 5.1 Real-time Cloud Sync (P1)
**Priority**: P1 - Should Have
**Estimated Time**: 15-20 hours
**User Impact**: High - Multi-device support

**Description**: Real-time synchronization across devices using Firebase

**Implementation**:
```kotlin
// build.gradle.kts
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
}

// FirestoreRepository.kt
@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String?
        get() = auth.currentUser?.uid

    fun syncWallets(): Flow<List<Wallet>> = callbackFlow {
        val listener = userId?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .collection("wallets")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val wallets = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject<Wallet>()
                    } ?: emptyList()

                    trySend(wallets)
                }
        }

        awaitClose { listener?.remove() }
    }

    suspend fun saveWallet(wallet: Wallet) {
        userId?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .collection("wallets")
                .document(wallet.id)
                .set(wallet)
                .await()
        }
    }
}

// Conflict Resolution
class SyncConflictResolver {
    fun resolve(local: Transaction, remote: Transaction): Transaction {
        // Last-write-wins strategy
        return if (local.updatedAt > remote.updatedAt) local else remote
    }
}
```

**Benefits**:
- Multi-device support
- Real-time updates
- No manual backup needed
- Better user experience

---

### 5.2 Export to Other Formats (P2)
**Priority**: P2 - Nice to Have
**Estimated Time**: 8-10 hours
**User Impact**: Medium - Better data portability

**Description**: Export to PDF, Excel, QuickBooks format

**PDF Export**:
```kotlin
// build.gradle.kts
dependencies {
    implementation("com.itextpdf:itext7-core:7.2.5")
}

// PdfExporter.kt
class PdfExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun exportTransactionsToPdf(
        transactions: List<TransactionWithCategory>,
        walletName: String
    ): Result<Uri> {
        return try {
            val file = File(context.getExternalFilesDir(null), "exports/${walletName}_${System.currentTimeMillis()}.pdf")
            file.parentFile?.mkdirs()

            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Add title
            document.add(
                Paragraph("Transaction Report - $walletName")
                    .setFontSize(20f)
                    .setBold()
            )

            // Add table
            val table = Table(UnitValue.createPercentArray(floatArrayOf(2f, 2f, 2f, 3f)))
            table.addHeaderCell("Date")
            table.addHeaderCell("Type")
            table.addHeaderCell("Amount")
            table.addHeaderCell("Description")

            transactions.forEach { txn ->
                table.addCell(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(txn.transaction.timestamp)))
                table.addCell(txn.transaction.type.name)
                table.addCell("$${txn.transaction.amount}")
                table.addCell(txn.transaction.description)
            }

            document.add(table)
            document.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Excel Export**:
```kotlin
// build.gradle.kts
dependencies {
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
}

// ExcelExporter.kt
class ExcelExporter {
    fun exportToExcel(transactions: List<TransactionWithCategory>): Result<Uri> {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Transactions")

        // Header row
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Date")
        headerRow.createCell(1).setCellValue("Type")
        headerRow.createCell(2).setCellValue("Amount")
        headerRow.createCell(3).setCellValue("Category")
        headerRow.createCell(4).setCellValue("Description")

        // Data rows
        transactions.forEachIndexed { index, txn ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(Date(txn.transaction.timestamp).toString())
            row.createCell(1).setCellValue(txn.transaction.type.name)
            row.createCell(2).setCellValue(txn.transaction.amount.toDouble())
            row.createCell(3).setCellValue(txn.category?.name ?: "")
            row.createCell(4).setCellValue(txn.transaction.description)
        }

        // Save and return URI
        // ...
    }
}
```

**Benefits**:
- Professional reporting
- Accounting software integration
- Better data portability
- Tax preparation support

---

## 6. Nice-to-Have Features

### 6.1 Home Screen Widgets (P3)
**Priority**: P3 - Nice to Have
**Estimated Time**: 8-10 hours
**User Impact**: Low-Medium

**Description**: Quick balance view and recent transactions widget

**Implementation**:
```kotlin
// BalanceWidgetProvider.kt
class BalanceWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BalanceWidget()
}

class BalanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Get wallet data
        val walletRepository = // Get from DI
        val totalBalance = walletRepository.getAllWallets().first()
            .sumOf { it.balance }

        provideContent {
            BalanceWidgetContent(totalBalance)
        }
    }
}

@Composable
fun BalanceWidgetContent(balance: BigDecimal) {
    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Text("Total Balance", style = TextStyle(fontSize = 14.sp))
            Text("$$balance", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))

            Button(
                text = "Add Transaction",
                onClick = actionStartActivity<MainActivity>()
            )
        }
    }
}
```

---

### 6.2 Shared Wallets (P3)
**Priority**: P3 - Future Enhancement
**Estimated Time**: 20+ hours
**User Impact**: High (for specific user segment)

**Description**: Share wallets with family members or partners

**Database Changes**:
```kotlin
@Entity(tableName = "wallet_members")
data class WalletMemberEntity(
    @PrimaryKey val id: String,
    val walletId: String,
    val userId: String,
    val role: String,  // OWNER, EDITOR, VIEWER
    val addedAt: Long
)

enum class WalletRole {
    OWNER,   // Full access
    EDITOR,  // Can add/edit transactions
    VIEWER   // Read-only access
}
```

---

### 6.3 Receipt Scanning (P3)
**Priority**: P3 - Future Enhancement
**Estimated Time**: 15-20 hours
**User Impact**: Medium

**Description**: Scan receipts and auto-fill transaction details using ML Kit

**Implementation**:
```kotlin
// build.gradle.kts
dependencies {
    implementation("com.google.mlkit:text-recognition:16.0.0")
}

// ReceiptScannerUseCase.kt
class ReceiptScannerUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun scanReceipt(imageUri: Uri): ReceiptData? {
        val image = InputImage.fromFilePath(context, imageUri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        return suspendCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val receiptData = parseReceiptText(visionText.text)
                    continuation.resume(receiptData)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }

    private fun parseReceiptText(text: String): ReceiptData {
        // Extract amount, merchant name, date
        val amountRegex = Regex("""\$?(\d+\.\d{2})""")
        val amount = amountRegex.find(text)?.groupValues?.get(1)?.toBigDecimalOrNull()

        // Extract date
        val dateRegex = Regex("""\d{1,2}/\d{1,2}/\d{2,4}""")
        val date = dateRegex.find(text)?.value

        return ReceiptData(
            amount = amount,
            merchantName = text.lines().firstOrNull(),
            date = date
        )
    }
}

data class ReceiptData(
    val amount: BigDecimal?,
    val merchantName: String?,
    val date: String?
)
```

---

### 6.4 Investment Tracking (P3)
**Priority**: P3 - Future Major Feature
**Estimated Time**: 30+ hours
**User Impact**: High (for investors)

**Description**: Track stocks, crypto, and other investments

**Database Schema**:
```kotlin
@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,  // STOCK, CRYPTO, REAL_ESTATE, etc.
    val symbol: String?,  // Stock ticker or crypto symbol
    val quantity: String,
    val purchasePrice: String,
    val currentPrice: String,
    val walletId: String,
    val purchaseDate: Long,
    val lastUpdated: Long
)

// Investment types
enum class InvestmentType {
    STOCK, CRYPTO, BOND, REAL_ESTATE, MUTUAL_FUND, ETF, COMMODITY
}
```

---

## 7. Implementation Roadmap

### Phase 1: Critical Security & Stability (Sprint 1-2)
**Duration**: 2-3 weeks
**Priority**: P0

- Database Encryption (4-6 hours)
- Database Migration System (3-4 hours)
- ProGuard Configuration (2-3 hours)
- Comprehensive Error Handling (4-5 hours)
- Retry Logic for Drive API (3-4 hours)

**Total Estimated Time**: 16-22 hours
**Team Size**: 1 developer
**Goal**: Production-ready security and stability

---

### Phase 2: Essential Features (Sprint 3-5)
**Duration**: 4-6 weeks
**Priority**: P0-P1

- Recurring Transactions (8-10 hours)
- Budget Management (10-12 hours)
- Bill Reminders (6-8 hours)
- Multi-Currency Exchange Rates (8-10 hours)
- Data Visualization & Charts (10-12 hours)

**Total Estimated Time**: 42-52 hours
**Team Size**: 1-2 developers
**Goal**: Feature parity with top finance apps

---

### Phase 3: UX Enhancements (Sprint 6-7)
**Duration**: 3-4 weeks
**Priority**: P1-P2

- Advanced Search Enhancement (4-5 hours)
- Spending Trends & Forecasting (8-10 hours)
- Onboarding Flow (6-8 hours)
- Quick Actions & Shortcuts (3-4 hours)
- Smart Categorization (12-15 hours)

**Total Estimated Time**: 33-42 hours
**Team Size**: 1-2 developers
**Goal**: Best-in-class user experience

---

### Phase 4: Cloud & Sync (Sprint 8-10)
**Duration**: 4-6 weeks
**Priority**: P1-P2

- Real-time Cloud Sync (15-20 hours)
- Export to Other Formats (8-10 hours)
- Conflict Resolution (6-8 hours)
- Offline Mode Improvements (4-6 hours)

**Total Estimated Time**: 33-44 hours
**Team Size**: 1-2 developers
**Goal**: Multi-device support and data portability

---

### Phase 5: Advanced Features (Sprint 11+)
**Duration**: 6-10 weeks
**Priority**: P2-P3

- Home Screen Widgets (8-10 hours)
- Shared Wallets (20+ hours)
- Receipt Scanning (15-20 hours)
- Investment Tracking (30+ hours)

**Total Estimated Time**: 73+ hours
**Team Size**: 2-3 developers
**Goal**: Differentiation and advanced capabilities

---

## 8. Success Metrics

### User Engagement Metrics
- Daily Active Users (DAU)
- Monthly Active Users (MAU)
- Session duration
- Transactions per user per month
- Feature adoption rate

### Financial Metrics
- Average transaction value
- Budgets created per user
- Budget compliance rate
- Cloud sync adoption rate

### Quality Metrics
- Crash-free rate (target: >99%)
- App load time (target: <2s)
- API response time (target: <500ms)
- User-reported bugs per release

### User Satisfaction
- App store rating (target: >4.5)
- Net Promoter Score (NPS)
- Feature request frequency
- User retention rate (30-day, 90-day)

---

## 9. Technical Debt Reduction

### Code Quality Improvements
1. **Increase Test Coverage** (Current: ~10%, Target: 70%)
   - Add UI tests with Compose testing
   - Add integration tests for DAOs
   - Add end-to-end tests for critical flows

2. **Documentation**
   - Add KDoc to all public APIs
   - Create architecture decision records
   - Document backup/restore process

3. **Performance Optimization**
   - Profile memory usage
   - Optimize database queries
   - Implement pagination for large lists

4. **Accessibility**
   - Add content descriptions
   - Screen reader support
   - High contrast mode

---

## 10. Conclusion

This comprehensive enhancement proposal outlines a clear path to transform Personal Wallet from a solid MVP into a production-ready, feature-rich personal finance application. The prioritized roadmap ensures critical security and stability issues are addressed first, followed by high-impact user features.

**Key Takeaways**:
- **Phase 1 (Security)** is essential before public release
- **Phase 2 (Features)** brings competitive parity
- **Phase 3 (UX)** provides differentiation
- **Phase 4 (Cloud)** enables scale
- **Phase 5 (Advanced)** creates market leadership

**Estimated Total Development Time**: 200+ hours over 6 months with 1-2 developers

**Next Steps**:
1. Review and prioritize features based on business goals
2. Allocate development resources
3. Set up project tracking (Jira, GitHub Projects)
4. Begin Phase 1 implementation
5. Gather user feedback after each phase
6. Iterate based on analytics and user feedback

---

**Document Version**: 1.0
**Last Updated**: November 26, 2025
**Prepared By**: Claude Code Analysis Team
**Status**: Ready for Review

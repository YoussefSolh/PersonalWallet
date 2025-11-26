# Personal Wallet

A comprehensive Android personal finance management application built with Kotlin, Jetpack Compose, and Clean Architecture principles.

## Business Overview

Personal Wallet is a mobile application designed to help users manage their personal finances efficiently. The app allows users to:

- Track multiple wallets (bank accounts, cash, credit cards, etc.)
- Record income and expense transactions
- Monitor spending across different categories
- View transaction history and wallet balances
- Secure data with biometric authentication
- Backup and sync data with Google Drive

### Key Features

1. **Multi-Wallet Management**: Create and manage unlimited wallets with different currencies
2. **Transaction Tracking**: Record income, expenses, and transfers between wallets
3. **Guest Mode**: Try the app without signing in
4. **Biometric Security**: Secure your financial data with fingerprint/face authentication
5. **Cloud Backup**: Automatic backup to Google Drive (when logged in)
6. **Category Management**: Organize transactions by customizable categories
7. **Real-time Balance Updates**: Instant balance calculations across all wallets

---

## Application Functionality Map

This section provides a complete map of all screens and user flows in the application.

### 1. Authentication Flow

```
┌─────────────────┐
│  Login Screen   │
│                 │
│  Options:       │
│  - Sign In      │
│  - Guest Mode   │
└────────┬────────┘
         │
         ├─ Sign In → Google Authentication → Dashboard
         │
         └─ Guest Mode → Dashboard (limited features)
```

**Screen**: `LoginScreen.kt`
**Route**: `login` (start destination)
**Purpose**: User authentication entry point

---

### 2. Dashboard (Home Screen)

```
┌──────────────────────────────┐
│      Dashboard Screen         │
│                               │
│  [Settings Icon]              │
│                               │
│  ┌────────────────────────┐  │
│  │   Total Balance        │  │
│  │   $X,XXX.XX           │  │
│  └────────────────────────┘  │
│                               │
│  Wallets:                     │
│  ┌────────────────────────┐  │
│  │ Wallet 1    $XXX.XX    │──┼─→ Click → Wallet Detail
│  └────────────────────────┘  │
│  ┌────────────────────────┐  │
│  │ Wallet 2    $XXX.XX    │──┼─→ Click → Wallet Detail
│  └────────────────────────┘  │
│                               │
│              [+] FAB          │──→ Add Wallet
└──────────────────────────────┘
```

**Screen**: `DashboardScreen.kt`
**Route**: `dashboard`
**Purpose**: Main hub showing all wallets and total balance

**Actions**:
- Click on any wallet → Navigate to Wallet Detail Screen
- Click [+] FAB button → Navigate to Add Wallet Screen
- Click Settings icon → Navigate to Settings Screen

---

### 3. Wallet Detail Screen

```
┌──────────────────────────────┐
│   [←] Wallet Name             │
│                               │
│  ┌────────────────────────┐  │
│  │  Current Balance       │  │
│  │  $X,XXX.XX            │  │
│  │  USD                   │  │
│  └────────────────────────┘  │
│                               │
│  Transactions:                │
│  ┌────────────────────────┐  │
│  │ Grocery Shopping       │  │
│  │ EXPENSE    -$50.00    │  │
│  └────────────────────────┘  │
│  ┌────────────────────────┐  │
│  │ Salary                 │  │
│  │ INCOME     +$2000.00  │  │
│  └────────────────────────┘  │
│                               │
│              [+] FAB          │──→ Add Transaction
└──────────────────────────────┘
```

**Screen**: `WalletDetailScreen.kt`
**Route**: `wallet_detail/{walletId}`
**Purpose**: View individual wallet balance and transaction history

**Actions**:
- Click [←] back button → Return to Dashboard
- Click [+] FAB button → Navigate to Add Transaction Screen
- View all transactions for this specific wallet

**This is where you ADD TRANSACTIONS**: Click the [+] button on a wallet detail screen!

---

### 4. Add Wallet Screen

```
┌──────────────────────────────┐
│   [←] Add Wallet              │
│                               │
│  ┌────────────────────────┐  │
│  │ Wallet Name            │  │
│  │ [Input Field]          │  │
│  └────────────────────────┘  │
│                               │
│  ┌────────────────────────┐  │
│  │ Initial Balance        │  │
│  │ $[Input Field]         │  │
│  └────────────────────────┘  │
│                               │
│  ┌────────────────────────┐  │
│  │ Currency               │  │
│  │ [Input Field: USD]     │  │
│  └────────────────────────┘  │
│                               │
│  ┌────────────────────────┐  │
│  │   Create Wallet        │  │
│  └────────────────────────┘  │
└──────────────────────────────┘
```

**Screen**: `AddWalletScreen.kt`
**Route**: `add_wallet`
**Purpose**: Create a new wallet

**Actions**:
- Fill in wallet name, initial balance, and currency
- Click "Create Wallet" → Wallet is created and user returns to Dashboard
- Click [←] back button → Return to Dashboard without creating

---

### 5. Add Transaction Screen

```
┌──────────────────────────────┐
│   [←] Add Transaction         │
│                               │
│  Transaction Type:            │
│  ┌──────────┐ ┌──────────┐  │
│  │ Income   │ │ Expense  │  │ ← Click to select
│  └──────────┘ └──────────┘  │
│                               │
│  ┌────────────────────────┐  │
│  │ Amount                 │  │
│  │ $[Input Field]         │  │
│  └────────────────────────┘  │
│                               │
│  ┌────────────────────────┐  │
│  │ Description            │  │
│  │ [Input Field]          │  │
│  └────────────────────────┘  │
│                               │
│  ┌────────────────────────┐  │
│  │  Add Transaction       │  │
│  └────────────────────────┘  │
└──────────────────────────────┘
```

**Screen**: `AddTransactionScreen.kt`
**Route**: `add_transaction/{walletId}`
**Purpose**: Add income or expense transaction to a specific wallet

**Actions**:
- Select transaction type (Income or Expense)
- Enter amount and description
- Click "Add Transaction" → Transaction is added and wallet balance updates
- Click [←] back button → Return to Wallet Detail without adding

---

### 6. Settings Screen

```
┌──────────────────────────────┐
│   [←] Settings                │
│                               │
│  ┌────────────────────────┐  │
│  │ Account                │  │
│  │ Manage your account    │  │
│  └────────────────────────┘  │
│                               │
│  ┌────────────────────────┐  │
│  │ Backup & Sync          │  │
│  │ Google Drive backup    │  │
│  └────────────────────────┘  │
│                               │
│  ┌────────────────────────┐  │
│  │ Security               │  │
│  │ Biometric settings     │  │
│  └────────────────────────┘  │
│                               │
│  ┌────────────────────────┐  │
│  │ Currency               │  │
│  │ Default: USD           │  │
│  └────────────────────────┘  │
│                               │
│  ┌────────────────────────┐  │
│  │ About                  │  │
│  │ Version 1.0            │  │
│  └────────────────────────┘  │
└──────────────────────────────┘
```

**Screen**: `SettingsScreen.kt`
**Route**: `settings`
**Purpose**: App configuration and preferences

**Actions**:
- Click [←] back button → Return to Dashboard
- Click on any setting item → Navigate to specific setting detail (TODO)

---

## Complete Navigation Flow

```
Login Screen (Start)
    │
    ├─→ Sign In → Dashboard
    └─→ Guest Mode → Dashboard
                        │
                        ├─→ Click Wallet → Wallet Detail Screen
                        │                       │
                        │                       └─→ [+] → Add Transaction Screen
                        │                                      │
                        │                                      └─→ Back to Wallet Detail
                        │
                        ├─→ [+] FAB → Add Wallet Screen
                        │                  │
                        │                  └─→ Back to Dashboard
                        │
                        └─→ Settings Icon → Settings Screen
                                              │
                                              └─→ Back to Dashboard
```

---

## User Journeys

### Journey 1: New User Sets Up First Wallet
1. Open app → Login Screen
2. Click "Continue as Guest"
3. Dashboard appears (empty state: "No wallets found")
4. Click [+] FAB button
5. Fill in wallet details (e.g., "Cash Wallet", $500, USD)
6. Click "Create Wallet"
7. Return to Dashboard → See new wallet

### Journey 2: Adding a Transaction
1. From Dashboard → Click on a wallet
2. Wallet Detail Screen opens
3. Click [+] FAB button
4. Select transaction type (Income/Expense)
5. Enter amount (e.g., $50) and description (e.g., "Grocery Shopping")
6. Click "Add Transaction"
7. Return to Wallet Detail → See new transaction and updated balance

### Journey 3: Viewing All Wallets
1. From Dashboard → View all wallets at once
2. See total balance across all wallets at the top
3. Each wallet shows name, currency, and current balance
4. Click any wallet to see its details

---

## Technical Architecture

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture (Domain, Data, Presentation layers)
- **Dependency Injection**: Hilt
- **Database**: Room
- **Async**: Coroutines + Flow
- **Navigation**: Jetpack Navigation Compose
- **Authentication**: Google Sign-In
- **Cloud Storage**: Google Drive API

### Project Structure
```
app/
├── domain/
│   ├── model/          # Data models (Wallet, Transaction, Category)
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Business logic use cases
├── data/
│   ├── local/          # Room database
│   ├── remote/         # Google Drive API
│   └── repository/     # Repository implementations
└── presentation/
    ├── ui/screen/      # Compose UI screens
    ├── viewmodel/      # ViewModels
    └── navigation/     # Navigation graph
```

---

## Build and Run

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 24+ (targetSdk 34)

### Setup
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Run the app on an emulator or physical device

### Gradle Configuration
- `compileSdk = 36`
- `minSdk = 24`
- `targetSdk = 34`

---

## Current Status

### ✅ Implemented Features

**Authentication & Security**
- ✅ Google Sign-In authentication
- ✅ Guest mode support
- ✅ Biometric authentication (fingerprint/face unlock)
- ✅ Sign out functionality

**Wallet Management**
- ✅ Create multiple wallets with different currencies
- ✅ Dashboard with wallet list and total balance
- ✅ View wallet details with transaction history
- ✅ Real-time balance calculations
- ✅ Delete wallets

**Transaction Management**
- ✅ Add transactions (Income/Expense/Transfer)
- ✅ Transfer money between wallets
- ✅ Edit transactions
- ✅ Delete transactions with balance recalculation
- ✅ Transaction search with debounce
- ✅ Filter transactions by type (Income/Expense/Transfer)
- ✅ Debt tracking on transfers
- ✅ Settle debts with reverse transactions

**Category Management**
- ✅ 16 predefined categories with icons
- ✅ View all categories (Income/Expense)
- ✅ Create custom categories
- ✅ Category-based transaction organization

**Debt Management**
- ✅ Mark transfers as debt
- ✅ View active debts list
- ✅ Settle debts automatically
- ✅ Track debt settlement history

**Reports & Analytics**
- ✅ Weekly/Monthly/Yearly reports
- ✅ Income vs Expense summary
- ✅ Net income calculation
- ✅ Spending by category with percentages
- ✅ Visual progress bars for spending
- ✅ Transaction count per category

**Backup & Sync**
- ✅ Google Drive backup integration
- ✅ Export data to JSON
- ✅ Restore data from backup
- ✅ Last backup timestamp tracking
- ✅ List available backups

**Testing**
- ✅ Unit tests for use cases
- ✅ Transaction logic tests
- ✅ UI integration tests
- ✅ Mockito + Coroutines test setup

---

## FAQ

**Q: Where do I add transactions?**
A: From the Dashboard, click on any wallet to open the Wallet Detail screen. Then click the [+] button at the bottom right to add a transaction to that wallet.

**Q: What's the difference between "Add Wallet" and "Add Transaction"?**
A:
- **Add Wallet**: Creates a new wallet/account (e.g., "Bank Account", "Cash", "Credit Card")
- **Add Transaction**: Records a financial activity (income or expense) within a specific wallet

**Q: Can I use the app without signing in?**
A: Yes! Click "Continue as Guest" on the login screen. Note that guest mode data is stored locally only and won't sync to the cloud.

**Q: How do I view all my transactions?**
A: Click on any wallet from the Dashboard. The Wallet Detail screen shows all transactions for that wallet.

---

## License

[Add your license here]

## Contributing

[Add contribution guidelines here]

## Support

For issues and feature requests, please open an issue on GitHub.

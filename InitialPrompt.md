### Project Goal
Build an **Android Wallet App** with an interactive **Jetpack Compose UI** and **Clean Architecture**.

### Architecture Rules
- Language: Kotlin
- UI: Jetpack Compose (Material3)
- Layers: domain, data, app (presentation)
- State management: ViewModel + Kotlin Flows
- Dependency Injection: Hilt
- Persistence: Room (encrypted if possible)
- Authentication: Google Sign-In (Gmail)
- Backup/Restore: Google Drive integration
- Testing: include unit + instrumentation tests

### Functional Requirements
- **Wallets (Money Locations)**: create multiple wallets (Home Safe, My Wallet, Wife’s Wallet, Friend Debt, Cards)
- **Transactions**: revenue, expense, wallet→wallet transfers
- **Debt Flag**: transfers can be marked as debt; allow settlement later
- **Categories**: predefined transaction categories with icons from a public font set; user can add more categories with custom icons
- **Backup**: save and restore data to Google Drive
- **Login**: Gmail login via Google Sign-In
- **Security**: optional biometric/app lock

### Behavior
- Adding a transaction updates balances immediately
- Wallet→wallet transfers adjust balances in both wallets
- Debt transfers tracked until cleared by return transaction
- Offline-first persistence; sync with Drive when online

### Deliverables — Generate Code For:
1. **Project structure**: domain, data, app modules
2. **Entities**: Wallet, Transaction, Category
3. **Use Cases**: AddTransactionUseCase, GetWalletBalanceUseCase
4. **Repository interfaces & implementations**
5. **Room setup**: DAOs, Database, Migrations
6. **DI setup** with Hilt
7. **ViewModels**: WalletListViewModel, TransactionViewModel
8. **Composables**: LoginScreen, DashboardScreen, WalletDetailScreen, AddTransactionScreen, CategoryScreen, SettingsScreen
9. **Navigation**: Compose Navigation setup
10. **Auth**: Google Sign-In integration (code + config)
11. **Backup/Restore**: Drive integration sample code
12. **Testing**: Unit test examples (use cases, repos), Instrumented UI test example

### Instructions for the Coding Agent
- Output Kotlin code snippets for each module and feature.
- Use modern Android conventions and Gradle Kotlin DSL.
- Ensure generated code compiles in Android Studio.
- Prefer simplicity and readability but keep Clean Architecture separation.
- Where full implementation is long, generate skeletons with TODOs for clarity.

Begin by generating the **Gradle settings and module structure**, then continue layer by layer.
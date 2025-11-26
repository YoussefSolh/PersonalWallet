# Testing Plan - Personal Wallet App
**Version**: 3.0.0
**Date**: November 26, 2025
**Features**: Dark Mode, Edit Wallet, Transaction Date Picker, Export to CSV, Edit Transaction

---

## 📋 Table of Contents
1. [Testing Overview](#testing-overview)
2. [Test Environment Setup](#test-environment-setup)
3. [Feature Testing](#feature-testing)
4. [Integration Testing](#integration-testing)
5. [Regression Testing](#regression-testing)
6. [Performance Testing](#performance-testing)
7. [Security Testing](#security-testing)
8. [Bug Tracking](#bug-tracking)

---

## Testing Overview

### Objectives
- ✅ Verify all 5 new features work as designed
- ✅ Ensure no existing functionality is broken
- ✅ Validate data integrity (wallet balances, transaction history)
- ✅ Test on multiple devices and Android versions
- ✅ Verify security implementations (FileProvider, input validation)

### Test Scope
- **In Scope**: All 5 new features, existing wallet/transaction operations, navigation
- **Out of Scope**: Google Sign-In (previously tested), Backup/Restore (unchanged)

### Testing Types
1. **Functional Testing** - Feature-by-feature validation
2. **Integration Testing** - Feature interactions
3. **Regression Testing** - Existing functionality
4. **Performance Testing** - App responsiveness
5. **Security Testing** - Data protection

---

## Test Environment Setup

### Prerequisites
1. **Build the APK**:
   ```bash
   cd "C:\GitHub\YoussefSolh\PersonalWallet"
   gradlew assembleDebug
   ```

2. **Install on Device/Emulator**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Prepare Test Data**:
   - Create 2-3 test wallets
   - Add 10-15 transactions (mix of INCOME, EXPENSE, TRANSFER)
   - Use various categories
   - Include transactions with special characters in descriptions

### Test Devices
- **Emulator**: Pixel 6 API 34 (Android 14)
- **Physical Device**: [Your test device, e.g., Samsung Galaxy S23, Android 13]
- **Minimum**: Android API 26 (Android 8.0)

### Test Accounts
- Use a test Google account for Sign-In
- No real financial data during testing

---

## Feature Testing

### 1. Dark Mode Toggle Testing

#### Test Case 1.1: Light Mode
**Steps**:
1. Launch app → Sign in
2. Navigate to Settings
3. Tap "Theme" → Select "Light"
4. Observe theme change

**Expected Results**:
- ✅ App switches to light theme immediately
- ✅ Status bar icons are dark
- ✅ All screens use light colors
- ✅ No flicker or layout shift
- ✅ Material3 dynamic colors applied (Android 12+)

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 1.2: Dark Mode
**Steps**:
1. Settings → Theme → Select "Dark"
2. Navigate through app screens

**Expected Results**:
- ✅ App switches to dark theme immediately
- ✅ Status bar icons are light
- ✅ All screens use dark colors
- ✅ No flicker or layout shift
- ✅ Text remains readable

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 1.3: System Mode
**Steps**:
1. Settings → Theme → Select "System"
2. Change device theme (Settings → Display → Dark theme)
3. Return to app

**Expected Results**:
- ✅ App matches device theme
- ✅ Theme updates when device theme changes
- ✅ No manual refresh needed

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 1.4: Theme Persistence
**Steps**:
1. Settings → Theme → Select "Dark"
2. Force close app (swipe away from recent apps)
3. Reopen app

**Expected Results**:
- ✅ App opens in dark theme
- ✅ Theme preference is retained

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

### 2. Edit Wallet Testing

#### Test Case 2.1: Edit Wallet Name
**Pre-condition**: Wallet "Main Wallet" exists with balance $1000

**Steps**:
1. Dashboard → Tap "Main Wallet"
2. Tap Edit icon (pencil) in top bar
3. Change name to "Personal Checking"
4. Tap "Update Wallet"

**Expected Results**:
- ✅ Form pre-fills with "Main Wallet", $1000, USD
- ✅ Title says "Edit Wallet"
- ✅ Balance label says "Current Balance" (not "Initial Balance")
- ✅ Button says "Update Wallet"
- ✅ Navigation back to Wallet Detail after save
- ✅ Name updates to "Personal Checking" in detail screen
- ✅ Name updates in dashboard wallet list
- ✅ Balance remains $1000

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 2.2: Edit Wallet Balance
**Pre-condition**: Wallet "Savings" exists with balance $5000

**Steps**:
1. Open "Savings" wallet
2. Tap Edit → Change balance to $5500
3. Save

**Expected Results**:
- ✅ Balance updates to $5500
- ✅ Dashboard shows new balance
- ✅ Wallet Detail shows new balance
- ✅ Transaction history unchanged

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 2.3: Edit Wallet Currency
**Steps**:
1. Open wallet → Edit
2. Change currency from "USD" to "EUR"
3. Save

**Expected Results**:
- ✅ Currency updates to EUR
- ✅ Displays correctly in wallet detail

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 2.4: Edit Wallet Validation
**Steps**:
1. Open wallet → Edit
2. Clear wallet name → Try to save
3. Enter negative balance → Try to save

**Expected Results**:
- ✅ Shows error: "Wallet name is required"
- ✅ Shows error: "Balance cannot be negative"
- ✅ Cannot save invalid data

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 2.5: Cancel Edit
**Steps**:
1. Open wallet → Edit
2. Change name and balance
3. Press Back button

**Expected Results**:
- ✅ Returns to Wallet Detail
- ✅ Changes are discarded
- ✅ Original values remain

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

### 3. Transaction Date Picker Testing

#### Test Case 3.1: Default Date
**Steps**:
1. Open wallet → Tap + (Add Transaction)
2. Observe Date section

**Expected Results**:
- ✅ Date card displays today's date
- ✅ Format: "Nov 26, 2025" (localized)
- ✅ Calendar icon visible

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 3.2: Select Past Date
**Steps**:
1. Add Transaction screen
2. Tap Date card
3. DatePicker dialog appears
4. Select yesterday
5. Tap "OK"
6. Fill other fields and save transaction

**Expected Results**:
- ✅ Material3 DatePicker appears
- ✅ Date updates to yesterday in card
- ✅ Transaction saves with selected date
- ✅ Transaction appears chronologically in list
- ✅ Transaction timestamp is correct (check in detailed view)

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 3.3: Select Future Date
**Steps**:
1. Add Transaction screen
2. Tap Date card
3. Select next week
4. Save transaction

**Expected Results**:
- ✅ Future date is selectable
- ✅ Transaction saves with future timestamp
- ✅ Appears in transaction list

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 3.4: Cancel Date Selection
**Steps**:
1. Add Transaction → Tap Date card
2. Select different date
3. Tap "Cancel"

**Expected Results**:
- ✅ DatePicker closes
- ✅ Original date remains unchanged

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

### 4. Export to CSV Testing

#### Test Case 4.1: Basic CSV Export
**Pre-condition**: Wallet with 5+ transactions

**Steps**:
1. Open wallet
2. Tap Export icon (download) in top bar
3. Select "Basic" format
4. Choose Email from share chooser
5. Check CSV attachment

**Expected Results**:
- ✅ Export dialog appears with "Basic" and "Detailed" options
- ✅ Android share chooser appears
- ✅ CSV file attached to email
- ✅ Filename: `transactions_[WalletName]_[Timestamp].csv`
- ✅ CSV format: `Date,Type,Amount,Category,Description,Wallet`
- ✅ All transactions included
- ✅ Success snackbar: "CSV exported successfully"

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 4.2: Detailed CSV Export
**Steps**:
1. Open wallet → Export → "Detailed"
2. Share to Google Drive

**Expected Results**:
- ✅ CSV has additional columns: Time, Currency, Category Color, Transaction ID
- ✅ File uploads to Drive successfully
- ✅ Can open file in Google Sheets

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 4.3: CSV Field Escaping
**Pre-condition**: Create transactions with:
- Description: `Lunch, with friends`
- Description: `Joe's Cafe`
- Description: `Multi
line
description`

**Steps**:
1. Export to CSV → Open in Excel/Sheets

**Expected Results**:
- ✅ Comma descriptions: `"Lunch, with friends"`
- ✅ Quote descriptions: `"Joe's Cafe"` or `"Joe""s Cafe"`
- ✅ Newline descriptions: Properly escaped
- ✅ CSV parses correctly in spreadsheet apps

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 4.4: Export Empty Wallet
**Pre-condition**: Wallet with zero transactions

**Steps**:
1. Open empty wallet
2. Tap Export icon

**Expected Results**:
- ✅ Export dialog appears
- ✅ Tap "Basic" or "Detailed"
- ✅ Snackbar shows: "No transactions to export"
- ✅ No share chooser appears

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 4.5: Multiple Exports
**Steps**:
1. Export once → Success
2. Export again immediately

**Expected Results**:
- ✅ Both exports succeed
- ✅ Two separate files created
- ✅ Files have different timestamps in names

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

### 5. Edit Transaction Testing

#### Test Case 5.1: Edit Transaction Amount
**Pre-condition**: Expense transaction $50 exists, wallet balance $1000

**Steps**:
1. Open wallet → Tap transaction
2. Tap "Edit" button
3. Change amount from $50 to $75
4. Save

**Expected Results**:
- ✅ Form pre-fills with all transaction data
- ✅ Title says "Edit Transaction"
- ✅ Button says "Update Transaction"
- ✅ Amount updates to $75
- ✅ Wallet balance changes: $1000 → $975 (decreased by additional $25)
- ✅ Transaction displays $75 in list

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 5.2: Edit Transaction Description
**Steps**:
1. Open transaction → Edit
2. Change description "Coffee" → "Coffee and bagel"
3. Save

**Expected Results**:
- ✅ Description updates
- ✅ Displays new description in transaction list

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 5.3: Edit Transaction Category
**Pre-condition**: Expense transaction with "Food & Dining" category

**Steps**:
1. Edit transaction
2. Change category to "Entertainment"
3. Save

**Expected Results**:
- ✅ Category updates
- ✅ Icon changes to Entertainment icon
- ✅ Category name displays "Entertainment"

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 5.4: Edit Transaction Date
**Steps**:
1. Edit transaction (dated today)
2. Change date to last week
3. Save

**Expected Results**:
- ✅ Transaction timestamp updates
- ✅ Transaction moves to correct chronological position in list
- ✅ Date displays correctly

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 5.5: Edit Transaction Type (Critical Test)
**Pre-condition**:
- Wallet "Main" with balance $1000
- Expense transaction -$100 (balance is $900)

**Steps**:
1. Edit the $100 expense
2. Change type from EXPENSE to INCOME
3. Keep amount $100
4. Save

**Expected Results**:
- ✅ Wallet balance calculation:
  - Reverse old: $900 + $100 = $1000 (undo expense)
  - Apply new: $1000 + $100 = $1100 (add income)
- ✅ Final wallet balance: $1100
- ✅ Transaction displays as INCOME with +$100
- ✅ Transaction color changes (red → green)

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 5.6: Edit Transfer Destination
**Pre-condition**:
- Wallet A: $1000
- Wallet B: $500
- Transfer $100 from A to B exists (A=$900, B=$600)

**Steps**:
1. Open Wallet A → Find transfer
2. Edit transfer
3. Change destination from Wallet B to Wallet C (balance $200)
4. Save

**Expected Results**:
- ✅ Wallet A: $900 (unchanged, still source)
- ✅ Wallet B: $500 (restored, no longer destination)
- ✅ Wallet C: $300 (received transfer)
- ✅ All balances accurate

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 5.7: Edit Validation
**Steps**:
1. Edit transaction
2. Clear description → Try to save
3. Set amount to 0 → Try to save

**Expected Results**:
- ✅ Shows error: "Description is required"
- ✅ Shows error: "Amount must be greater than zero"
- ✅ Cannot save invalid data

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

#### Test Case 5.8: Cancel Edit
**Steps**:
1. Edit transaction
2. Change amount, description, category
3. Press Back button

**Expected Results**:
- ✅ Returns to Wallet Detail
- ✅ No changes applied
- ✅ Original transaction data intact

**Status**: [ ] Pass [ ] Fail
**Notes**: _______________________

---

## Integration Testing

### IT-1: Dark Mode + All Screens
**Steps**:
1. Enable Dark Mode
2. Navigate through all screens: Dashboard, Wallet Detail, Add Transaction, Settings, Reports, etc.

**Expected Results**:
- ✅ All screens respect dark theme
- ✅ No white flashes or mixed themes

**Status**: [ ] Pass [ ] Fail

---

### IT-2: Edit Wallet + Transactions
**Steps**:
1. Wallet has 5 transactions
2. Edit wallet name
3. Navigate to transactions

**Expected Results**:
- ✅ All transactions still associated with wallet
- ✅ Transaction list displays correctly

**Status**: [ ] Pass [ ] Fail

---

### IT-3: Edit Transaction + Export CSV
**Steps**:
1. Edit a transaction (change amount)
2. Export to CSV
3. Check CSV contents

**Expected Results**:
- ✅ CSV contains updated transaction data
- ✅ Updated amount reflected in export

**Status**: [ ] Pass [ ] Fail

---

### IT-4: Date Picker + Edit Transaction
**Steps**:
1. Add transaction with custom date (last week)
2. Edit same transaction
3. Change date to different day

**Expected Results**:
- ✅ Form shows original custom date
- ✅ Can change to new date
- ✅ Transaction updates correctly

**Status**: [ ] Pass [ ] Fail

---

### IT-5: Multiple Edits Sequence
**Steps**:
1. Edit wallet name
2. Add transaction with custom date
3. Edit transaction amount
4. Edit transaction category
5. Export to CSV

**Expected Results**:
- ✅ All operations succeed
- ✅ Data remains consistent
- ✅ CSV reflects final state

**Status**: [ ] Pass [ ] Fail

---

## Regression Testing

### RT-1: Create Wallet
**Steps**: Create new wallet

**Expected**: Works as before, no issues

**Status**: [ ] Pass [ ] Fail

---

### RT-2: Add Transaction
**Steps**: Add INCOME, EXPENSE, TRANSFER

**Expected**: All types work correctly

**Status**: [ ] Pass [ ] Fail

---

### RT-3: Delete Transaction
**Steps**: Delete transaction, check wallet balance

**Expected**: Balance updates correctly

**Status**: [ ] Pass [ ] Fail

---

### RT-4: Search Transactions
**Steps**: Use search bar in Wallet Detail

**Expected**: Search works correctly

**Status**: [ ] Pass [ ] Fail

---

### RT-5: Filter Transactions
**Steps**: Filter by type (INCOME, EXPENSE, TRANSFER)

**Expected**: Filtering works correctly

**Status**: [ ] Pass [ ] Fail

---

### RT-6: Transfer Between Wallets
**Steps**: Create transfer, verify both wallet balances

**Expected**: Both wallets update correctly

**Status**: [ ] Pass [ ] Fail

---

### RT-7: Google Sign-In
**Steps**: Sign out, sign in again

**Expected**: Sign-in works (no changes made to auth)

**Status**: [ ] Pass [ ] Fail

---

### RT-8: Categories Management
**Steps**: Add category, edit category

**Expected**: Categories work as before

**Status**: [ ] Pass [ ] Fail

---

## Performance Testing

### P-1: App Launch Time
**Steps**: Cold start app, measure time to Dashboard

**Expected**: < 3 seconds

**Actual**: _______ seconds

**Status**: [ ] Pass [ ] Fail

---

### P-2: Theme Switch Performance
**Steps**: Toggle between Light/Dark 5 times

**Expected**: Instant (< 100ms), no lag

**Status**: [ ] Pass [ ] Fail

---

### P-3: Large Transaction List
**Steps**:
1. Create wallet with 100 transactions
2. Scroll through list
3. Export to CSV

**Expected**:
- Smooth scrolling (60 FPS)
- Export completes in < 5 seconds

**Status**: [ ] Pass [ ] Fail

---

### P-4: Multiple Wallet Navigation
**Steps**: Switch between 10 wallets rapidly

**Expected**: No lag, instant loading

**Status**: [ ] Pass [ ] Fail

---

## Security Testing

### S-1: FileProvider Security
**Steps**:
1. Export CSV
2. Check file permissions with `adb shell`

**Expected**:
- ✅ File in app's external files directory
- ✅ Not world-readable
- ✅ FileProvider URI (content://)

**Status**: [ ] Pass [ ] Fail

---

### S-2: Input Validation
**Steps**: Try SQL injection, XSS in text fields

**Expected**: All inputs sanitized

**Status**: [ ] Pass [ ] Fail

---

### S-3: Data Persistence Security
**Steps**: Check DataStore and Room database files

**Expected**:
- ✅ DataStore encrypted (default)
- ✅ Database in private app directory

**Status**: [ ] Pass [ ] Fail

---

## Bug Tracking

### Bugs Found

| # | Feature | Description | Severity | Status |
|---|---------|-------------|----------|--------|
| 1 |         |             |          |        |
| 2 |         |             |          |        |
| 3 |         |             |          |        |

### Severity Levels
- **Critical**: App crash, data loss
- **High**: Feature broken, major UX issue
- **Medium**: Minor functionality issue
- **Low**: Cosmetic issue

---

## Test Summary

**Date Tested**: _________________
**Tester**: _________________
**Device**: _________________
**Android Version**: _________________

### Overall Results
- **Total Test Cases**: 50+
- **Passed**: _____ / _____
- **Failed**: _____ / _____
- **Blocked**: _____ / _____
- **Pass Rate**: _____%

### Recommendation
[ ] **Ready for Production** - All critical tests pass
[ ] **Needs Minor Fixes** - Low severity issues only
[ ] **Needs Major Fixes** - High severity issues found
[ ] **Blocked** - Cannot proceed with testing

### Sign-off
**Tester Signature**: _________________
**Date**: _________________

---

**Notes**: Use this document as a checklist. Mark each test case as you complete it. Document any bugs in the Bug Tracking section with clear reproduction steps.

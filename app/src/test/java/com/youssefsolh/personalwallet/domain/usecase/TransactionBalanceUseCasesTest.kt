package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.testutil.InMemoryWalletStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Covers AddTransactionUseCase, UpdateTransactionUseCase, DeleteTransactionUseCase and
 * SettleDebtUseCase against an in-memory store, asserting on resulting balances.
 */
class TransactionBalanceUseCasesTest {

    private lateinit var store: InMemoryWalletStore
    private lateinit var add: AddTransactionUseCase
    private lateinit var update: UpdateTransactionUseCase
    private lateinit var delete: DeleteTransactionUseCase
    private lateinit var settle: SettleDebtUseCase

    @Before
    fun setup() {
        store = InMemoryWalletStore()
        store.addCurrency("USD", "1", isDefault = true)
        store.addCurrency("EUR", "0.92")
        store.addWallet("usd", "100.00", "USD")
        store.addWallet("usd2", "0.00", "USD")
        store.addWallet("eur", "0.00", "EUR")

        val updater = WalletBalanceUpdater(store.walletRepository, store.currencyRepository)
        add = AddTransactionUseCase(store.transactionRepository, updater, store.runner)
        update = UpdateTransactionUseCase(store.transactionRepository, updater, store.runner)
        delete = DeleteTransactionUseCase(store.transactionRepository, updater, store.runner)
        settle = SettleDebtUseCase(store.transactionRepository, add, store.runner)
    }

    private fun tx(
        id: String,
        type: TransactionType,
        amount: String,
        from: String? = null,
        to: String? = null,
        isDebt: Boolean = false
    ) = Transaction(
        id = id,
        amount = BigDecimal(amount),
        type = type,
        description = id,
        categoryId = "cat",
        fromWalletId = from,
        toWalletId = to,
        isDebt = isDebt
    )

    private fun assertBalance(expected: String, walletId: String) {
        assertEquals(0, BigDecimal(expected).compareTo(store.balanceOf(walletId)))
    }

    @Test
    fun `income increases and expense decreases balance`() = runTest {
        assertTrue(add(tx("i", TransactionType.INCOME, "50", to = "usd")).isSuccess)
        assertTrue(add(tx("e", TransactionType.EXPENSE, "30", from = "usd")).isSuccess)
        assertBalance("120", "usd")
    }

    @Test
    fun `same-currency transfer moves the amount`() = runTest {
        add(tx("t", TransactionType.TRANSFER, "40", from = "usd", to = "usd2")).getOrThrow()
        assertBalance("60", "usd")
        assertBalance("40", "usd2")
        assertEquals(0, BigDecimal("40").compareTo(store.transactions.getValue("t").destinationAmount))
    }

    @Test
    fun `cross-currency transfer converts and records credited amount`() = runTest {
        add(tx("t", TransactionType.TRANSFER, "100", from = "usd", to = "eur")).getOrThrow()
        assertBalance("0", "usd")
        assertBalance("92.00", "eur")
        assertEquals(0, BigDecimal("92.00").compareTo(store.transactions.getValue("t").destinationAmount))
    }

    @Test
    fun `deleting a cross-currency transfer restores both balances exactly`() = runTest {
        val transfer = tx("t", TransactionType.TRANSFER, "100", from = "usd", to = "eur")
        add(transfer).getOrThrow()

        delete(transfer).getOrThrow()

        assertBalance("100", "usd")
        assertBalance("0", "eur")
        assertTrue(store.transactions.getValue("t").isDeleted)
    }

    @Test
    fun `deleting twice does not reverse twice`() = runTest {
        val expense = tx("e", TransactionType.EXPENSE, "30", from = "usd")
        add(expense).getOrThrow()
        delete(expense).getOrThrow()
        delete(expense).getOrThrow()
        assertBalance("100", "usd")
    }

    @Test
    fun `editing only the description of a cross-currency transfer keeps balances unchanged`() = runTest {
        val transfer = tx("t", TransactionType.TRANSFER, "100", from = "usd", to = "eur")
        add(transfer).getOrThrow()
        // Rates move after the transfer was made
        store.addCurrency("EUR", "0.50")

        update(transfer, transfer.copy(description = "renamed")).getOrThrow()

        assertBalance("0", "usd")
        assertBalance("92.00", "eur")
    }

    @Test
    fun `changing a cross-currency transfer amount reconverts at current rate`() = runTest {
        val transfer = tx("t", TransactionType.TRANSFER, "100", from = "usd", to = "eur")
        add(transfer).getOrThrow()

        update(transfer, transfer.copy(amount = BigDecimal("50"))).getOrThrow()

        assertBalance("50", "usd")
        assertBalance("46.00", "eur")
    }

    @Test
    fun `update expense amount adjusts balance`() = runTest {
        val expense = tx("e", TransactionType.EXPENSE, "30", from = "usd")
        add(expense).getOrThrow()
        update(expense, expense.copy(amount = BigDecimal("45"))).getOrThrow()
        assertBalance("55", "usd")
    }

    @Test
    fun `changing type from expense to income adjusts balance`() = runTest {
        val expense = tx("e", TransactionType.EXPENSE, "30", from = "usd")
        add(expense).getOrThrow()
        update(
            expense,
            expense.copy(type = TransactionType.INCOME, fromWalletId = null, toWalletId = "usd")
        ).getOrThrow()
        assertBalance("130", "usd")
    }

    @Test
    fun `changing transfer destination moves credit to the new wallet`() = runTest {
        val transfer = tx("t", TransactionType.TRANSFER, "40", from = "usd", to = "usd2")
        add(transfer).getOrThrow()
        update(transfer, transfer.copy(toWalletId = "eur")).getOrThrow()
        assertBalance("60", "usd")
        assertBalance("0", "usd2")
        assertBalance("36.80", "eur")
    }

    @Test
    fun `update of unknown transaction fails without touching balances`() = runTest {
        val ghost = tx("ghost", TransactionType.EXPENSE, "30", from = "usd")
        assertTrue(update(ghost, ghost.copy(amount = BigDecimal("10"))).isFailure)
        assertBalance("100", "usd")
    }

    @Test
    fun `failure mid-transfer rolls back the insert and the debit`() = runTest {
        val transfer = tx("t", TransactionType.TRANSFER, "40", from = "usd", to = "usd2")
        // Debit succeeds, credit (second wallet update) fails
        store.failWalletUpdateAfter = 2

        assertTrue(add(transfer).isFailure)

        assertBalance("100", "usd")
        assertBalance("0", "usd2")
        assertFalse(store.transactions.containsKey("t"))
    }

    @Test
    fun `settling a cross-currency debt returns the original amounts`() = runTest {
        val debt = tx("d", TransactionType.TRANSFER, "100", from = "usd", to = "eur", isDebt = true)
        add(debt).getOrThrow()
        // Rates move before settlement
        store.addCurrency("EUR", "0.50")

        settle(store.transactions.getValue("d")).getOrThrow()

        assertBalance("100", "usd")
        assertBalance("0", "eur")
        assertTrue(store.transactions.getValue("d").debtSettled)
    }

    @Test
    fun `failed settlement leaves debt unsettled`() = runTest {
        val debt = tx("d", TransactionType.TRANSFER, "40", from = "usd", to = "usd2", isDebt = true)
        add(debt).getOrThrow()
        store.failNextWalletUpdate = true

        assertTrue(settle(store.transactions.getValue("d")).isFailure)

        assertFalse(store.transactions.getValue("d").debtSettled)
        assertBalance("60", "usd")
        assertBalance("40", "usd2")
        assertEquals(1, store.transactions.size)
    }
}

package com.youssefsolh.personalwallet.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import android.util.Log
import com.youssefsolh.personalwallet.data.local.dao.CategoryDao
import com.youssefsolh.personalwallet.data.local.dao.CurrencyDao
import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.data.local.dao.WalletDao
import com.youssefsolh.personalwallet.data.local.entity.CategoryEntity
import com.youssefsolh.personalwallet.data.local.entity.CurrencyEntity
import com.youssefsolh.personalwallet.data.local.entity.TransactionEntity
import com.youssefsolh.personalwallet.data.local.entity.WalletEntity
import com.youssefsolh.personalwallet.domain.model.DefaultCategories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        CurrencyEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun currencyDao(): CurrencyDao

    companion object {
        private const val TAG = "WalletDatabase"
        const val DATABASE_NAME = "wallet_database"

        fun buildDatabase(
            context: Context,
            factory: androidx.sqlite.db.SupportSQLiteOpenHelper.Factory? = null
        ): WalletDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                WalletDatabase::class.java,
                DATABASE_NAME
            )

            // Apply encryption factory if provided
            if (factory != null) {
                builder.openHelperFactory(factory)
            }

            return builder
                // Use TRUNCATE journal mode for better compatibility with SQLCipher
                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        try {
                            Log.i(TAG, "Database onCreate: Creating new database")

                            // Insert default categories on database creation using SQL
                            // Do NOT set page_size here - it must be set before database creation
                            val timestamp = System.currentTimeMillis()
                            var categoryCount = 0

                            DefaultCategories.getDefaultCategories().forEach { category ->
                                db.execSQL("""
                                    INSERT OR IGNORE INTO categories
                                    (id, userId, name, icon, color, type, isDefault, isCustom, createdAt, updatedAt, isDeleted)
                                    VALUES (?, 'default', ?, ?, ?, ?, 1, 0, ?, ?, 0)
                                """, arrayOf(
                                    category.id,
                                    category.name,
                                    category.icon,
                                    category.color,
                                    category.type.name,
                                    timestamp,
                                    timestamp
                                ))
                                categoryCount++
                            }

                            Log.i(TAG, "onCreate: Successfully inserted $categoryCount default categories")

                            // Insert default currencies on database creation
                            // USD as system currency (cannot be deleted, is default)
                            db.execSQL("""
                                INSERT OR IGNORE INTO currencies
                                (code, name, symbol, exchange_rate_to_default, is_default, is_system_currency, is_manual_rate, created_at, updated_at, last_rate_update)
                                VALUES ('USD', 'US Dollar', '$', '1.0', 1, 1, 0, $timestamp, $timestamp, $timestamp)
                            """)
                            // EUR as regular currency (can be deleted)
                            db.execSQL("""
                                INSERT OR IGNORE INTO currencies
                                (code, name, symbol, exchange_rate_to_default, is_default, is_system_currency, is_manual_rate, created_at, updated_at, last_rate_update)
                                VALUES ('EUR', 'Euro', '€', '0.92', 0, 0, 0, $timestamp, $timestamp, $timestamp)
                            """)

                            Log.i(TAG, "onCreate: Successfully inserted default currencies (USD, EUR)")
                        } catch (e: Exception) {
                            Log.e(TAG, "onCreate FAILED: ${e.message}", e)
                            throw e
                        }
                    }
                })
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .build()
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    Log.i(TAG, "Starting MIGRATION_1_2: Adding userId columns")

                    // Add userId column to wallets table with ORPHANED marker
                    database.execSQL("ALTER TABLE wallets ADD COLUMN userId TEXT NOT NULL DEFAULT 'ORPHANED_DATA'")
                    Log.d(TAG, "MIGRATION_1_2: Added userId to wallets table")

                    // Add userId column to transactions table with ORPHANED marker
                    database.execSQL("ALTER TABLE transactions ADD COLUMN userId TEXT NOT NULL DEFAULT 'ORPHANED_DATA'")
                    Log.d(TAG, "MIGRATION_1_2: Added userId to transactions table")

                    // Add userId column to categories table with ORPHANED marker (except defaults)
                    database.execSQL("ALTER TABLE categories ADD COLUMN userId TEXT NOT NULL DEFAULT 'ORPHANED_DATA'")
                    Log.d(TAG, "MIGRATION_1_2: Added userId to categories table")

                    // Update default categories to use 'default' userId
                    database.execSQL("UPDATE categories SET userId = 'default' WHERE isDefault = 1")
                    Log.d(TAG, "MIGRATION_1_2: Updated default categories userId")

                    Log.i(TAG, "MIGRATION_1_2 completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "MIGRATION_1_2 FAILED: ${e.message}", e)
                    throw e
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    Log.i(TAG, "Starting MIGRATION_2_3: Adding default categories")
                    val timestamp = System.currentTimeMillis()

                    // Insert ALL default categories for existing users
                    // This ensures users migrating from v2 get all default categories, not just new ones

                    // Original default income categories
                    val originalIncomeCategories = listOf(
                    "('income_salary', 'default', 'Salary', '💼', '#4CAF50', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_freelance', 'default', 'Freelance', '💻', '#2196F3', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_investment', 'default', 'Investment', '📈', '#FF9800', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_gift', 'default', 'Gift', '🎁', '#E91E63', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_bonus', 'default', 'Bonus', '🎉', '#9C27B0', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_rental', 'default', 'Rental Income', '🏠', '#795548', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_refund', 'default', 'Refund', '↩️', '#607D8B', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_other', 'default', 'Other Income', '💰', '#00BCD4', 'INCOME', 1, 0, $timestamp, $timestamp, 0)"
                )

                // Original default expense categories
                val originalExpenseCategories = listOf(
                    "('expense_rent', 'default', 'Rent/Mortgage', '🏠', '#795548', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_food', 'default', 'Food & Dining', '🍽️', '#FF5722', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_groceries', 'default', 'Groceries', '🛒', '#4CAF50', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_transport', 'default', 'Transportation', '🚗', '#2196F3', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_utilities', 'default', 'Utilities', '⚡', '#FFC107', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_entertainment', 'default', 'Entertainment', '🎬', '#9C27B0', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_shopping', 'default', 'Shopping', '🛍️', '#E91E63', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_health', 'default', 'Health & Medical', '🏥', '#F44336', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_education', 'default', 'Education', '📚', '#3F51B5', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_travel', 'default', 'Travel', '✈️', '#00BCD4', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_bills', 'default', 'Bills', '🧾', '#607D8B', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_fitness', 'default', 'Fitness & Sports', '🏋️', '#FF6F00', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_pets', 'default', 'Pets', '🐾', '#8D6E63', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_home', 'default', 'Home & Garden', '🏡', '#689F38', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_subscriptions', 'default', 'Subscriptions', '📱', '#5E35B1', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_insurance', 'default', 'Insurance', '🛡️', '#1565C0', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_personal_care', 'default', 'Personal Care', '💇', '#EC407A', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_clothing', 'default', 'Clothing', '👔', '#AB47BC', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_fuel', 'default', 'Gas & Fuel', '⛽', '#EF6C00', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_internet', 'default', 'Internet & Phone', '📡', '#0097A7', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_coffee', 'default', 'Coffee & Snacks', '☕', '#6D4C41', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_gifts', 'default', 'Gifts Given', '🎁', '#D81B60', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_charity', 'default', 'Charity & Donations', '❤️', '#C62828', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_car_maintenance', 'default', 'Car Maintenance', '🔧', '#455A64', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_restaurants', 'default', 'Restaurants & Bars', '🍷', '#6A1B9A', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_loans', 'default', 'Loans & Debt', '🏦', '#424242', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_savings', 'default', 'Savings', '🐷', '#F48FB1', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_children', 'default', 'Children & Childcare', '👶', '#FFB74D', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_business', 'default', 'Business Expenses', '💼', '#5D4037', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_parking', 'default', 'Parking & Tolls', '🅿️', '#78909C', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_laundry', 'default', 'Laundry & Cleaning', '🧺', '#64B5F6', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_books', 'default', 'Books & Media', '📖', '#9575CD', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_furniture', 'default', 'Furniture & Appliances', '🛋️', '#A1887F', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_taxes', 'default', 'Taxes', '📋', '#757575', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_other', 'default', 'Other Expense', '💸', '#9E9E9E', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)"
                )

                // Insert all income categories
                var incomeCount = 0
                originalIncomeCategories.forEach { values ->
                    database.execSQL("""
                        INSERT OR IGNORE INTO categories
                        (id, userId, name, icon, color, type, isDefault, isCustom, createdAt, updatedAt, isDeleted)
                        VALUES $values
                    """)
                    incomeCount++
                }
                Log.d(TAG, "MIGRATION_2_3: Inserted $incomeCount income categories")

                // Insert all expense categories
                var expenseCount = 0
                originalExpenseCategories.forEach { values ->
                    database.execSQL("""
                        INSERT OR IGNORE INTO categories
                        (id, userId, name, icon, color, type, isDefault, isCustom, createdAt, updatedAt, isDeleted)
                        VALUES $values
                    """)
                    expenseCount++
                }
                Log.d(TAG, "MIGRATION_2_3: Inserted $expenseCount expense categories")

                    Log.i(TAG, "MIGRATION_2_3 completed successfully - Total categories: ${incomeCount + expenseCount}")
                } catch (e: Exception) {
                    Log.e(TAG, "MIGRATION_2_3 FAILED: ${e.message}", e)
                    throw e
                }
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    Log.i(TAG, "Starting MIGRATION_3_4: Ensuring all default categories exist")

                    // For users upgrading from v3, ensure ALL default categories exist
                    // This fixes the issue where users who upgraded to v3 before the fix
                    // didn't receive all default categories
                    val timestamp = System.currentTimeMillis()

                    // All default income categories
                    val allIncomeCategories = listOf(
                    "('income_salary', 'default', 'Salary', '💼', '#4CAF50', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_freelance', 'default', 'Freelance', '💻', '#2196F3', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_investment', 'default', 'Investment', '📈', '#FF9800', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_gift', 'default', 'Gift', '🎁', '#E91E63', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_bonus', 'default', 'Bonus', '🎉', '#9C27B0', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_rental', 'default', 'Rental Income', '🏠', '#795548', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_refund', 'default', 'Refund', '↩️', '#607D8B', 'INCOME', 1, 0, $timestamp, $timestamp, 0)",
                    "('income_other', 'default', 'Other Income', '💰', '#00BCD4', 'INCOME', 1, 0, $timestamp, $timestamp, 0)"
                )

                // All default expense categories
                val allExpenseCategories = listOf(
                    "('expense_rent', 'default', 'Rent/Mortgage', '🏠', '#795548', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_food', 'default', 'Food & Dining', '🍽️', '#FF5722', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_groceries', 'default', 'Groceries', '🛒', '#4CAF50', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_transport', 'default', 'Transportation', '🚗', '#2196F3', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_utilities', 'default', 'Utilities', '⚡', '#FFC107', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_entertainment', 'default', 'Entertainment', '🎬', '#9C27B0', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_shopping', 'default', 'Shopping', '🛍️', '#E91E63', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_health', 'default', 'Health & Medical', '🏥', '#F44336', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_education', 'default', 'Education', '📚', '#3F51B5', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_travel', 'default', 'Travel', '✈️', '#00BCD4', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_bills', 'default', 'Bills', '🧾', '#607D8B', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_fitness', 'default', 'Fitness & Sports', '🏋️', '#FF6F00', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_pets', 'default', 'Pets', '🐾', '#8D6E63', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_home', 'default', 'Home & Garden', '🏡', '#689F38', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_subscriptions', 'default', 'Subscriptions', '📱', '#5E35B1', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_insurance', 'default', 'Insurance', '🛡️', '#1565C0', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_personal_care', 'default', 'Personal Care', '💇', '#EC407A', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_clothing', 'default', 'Clothing', '👔', '#AB47BC', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_fuel', 'default', 'Gas & Fuel', '⛽', '#EF6C00', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_internet', 'default', 'Internet & Phone', '📡', '#0097A7', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_coffee', 'default', 'Coffee & Snacks', '☕', '#6D4C41', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_gifts', 'default', 'Gifts Given', '🎁', '#D81B60', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_charity', 'default', 'Charity & Donations', '❤️', '#C62828', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_car_maintenance', 'default', 'Car Maintenance', '🔧', '#455A64', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_restaurants', 'default', 'Restaurants & Bars', '🍷', '#6A1B9A', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_loans', 'default', 'Loans & Debt', '🏦', '#424242', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_savings', 'default', 'Savings', '🐷', '#F48FB1', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_children', 'default', 'Children & Childcare', '👶', '#FFB74D', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_business', 'default', 'Business Expenses', '💼', '#5D4037', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_parking', 'default', 'Parking & Tolls', '🅿️', '#78909C', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_laundry', 'default', 'Laundry & Cleaning', '🧺', '#64B5F6', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_books', 'default', 'Books & Media', '📖', '#9575CD', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_furniture', 'default', 'Furniture & Appliances', '🛋️', '#A1887F', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_taxes', 'default', 'Taxes', '📋', '#757575', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)",
                    "('expense_other', 'default', 'Other Expense', '💸', '#9E9E9E', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)"
                )

                // Insert all income categories (INSERT OR IGNORE prevents duplicates)
                var incomeCount = 0
                allIncomeCategories.forEach { values ->
                    database.execSQL("""
                        INSERT OR IGNORE INTO categories
                        (id, userId, name, icon, color, type, isDefault, isCustom, createdAt, updatedAt, isDeleted)
                        VALUES $values
                    """)
                    incomeCount++
                }
                Log.d(TAG, "MIGRATION_3_4: Processed $incomeCount income categories")

                // Insert all expense categories (INSERT OR IGNORE prevents duplicates)
                var expenseCount = 0
                allExpenseCategories.forEach { values ->
                    database.execSQL("""
                        INSERT OR IGNORE INTO categories
                        (id, userId, name, icon, color, type, isDefault, isCustom, createdAt, updatedAt, isDeleted)
                        VALUES $values
                    """)
                    expenseCount++
                }
                Log.d(TAG, "MIGRATION_3_4: Processed $expenseCount expense categories")

                    // Count actual categories in database
                    val cursor = database.query("SELECT COUNT(*) FROM categories WHERE userId = 'default' AND isDeleted = 0")
                    cursor.moveToFirst()
                    val totalCount = cursor.getInt(0)
                    cursor.close()

                    Log.i(TAG, "MIGRATION_3_4 completed successfully - Total default categories in DB: $totalCount")
                } catch (e: Exception) {
                    Log.e(TAG, "MIGRATION_3_4 FAILED: ${e.message}", e)
                    throw e
                }
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    Log.i(TAG, "Starting MIGRATION_4_5: Fixing default categories")

                    val timestamp = System.currentTimeMillis()

                    // Define all default categories using DefaultCategories data
                    val defaultCategories = DefaultCategories.getDefaultCategories()

                    // Insert all default categories (INSERT OR REPLACE ensures they exist)
                    var insertedCount = 0
                    defaultCategories.forEach { category ->
                        database.execSQL("""
                            INSERT OR REPLACE INTO categories
                            (id, userId, name, icon, color, type, isDefault, isCustom, createdAt, updatedAt, isDeleted)
                            VALUES (?, 'default', ?, ?, ?, ?, 1, 0, ?, ?, 0)
                        """, arrayOf(
                            category.id,
                            category.name,
                            category.icon,
                            category.color,
                            category.type.name,
                            timestamp,
                            timestamp
                        ))
                        insertedCount++
                    }

                    Log.d(TAG, "MIGRATION_4_5: Inserted/updated $insertedCount default categories")

                    // Count actual categories in database
                    val cursor = database.query("SELECT COUNT(*) FROM categories WHERE userId = 'default' AND isDeleted = 0")
                    cursor.moveToFirst()
                    val totalCount = cursor.getInt(0)
                    cursor.close()

                    Log.i(TAG, "MIGRATION_4_5 completed successfully - Total default categories in DB: $totalCount")
                } catch (e: Exception) {
                    Log.e(TAG, "MIGRATION_4_5 FAILED: ${e.message}", e)
                    throw e
                }
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    Log.i(TAG, "Starting MIGRATION_5_6: Adding multi-currency support")
                    val timestamp = System.currentTimeMillis()

                    // 1. Create currencies table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS currencies (
                            code TEXT PRIMARY KEY NOT NULL,
                            name TEXT NOT NULL,
                            symbol TEXT NOT NULL,
                            exchange_rate_to_default TEXT NOT NULL,
                            is_default INTEGER NOT NULL DEFAULT 0,
                            is_system_currency INTEGER NOT NULL DEFAULT 0,
                            is_manual_rate INTEGER NOT NULL DEFAULT 0,
                            created_at INTEGER NOT NULL,
                            updated_at INTEGER NOT NULL,
                            last_rate_update INTEGER
                        )
                    """)
                    Log.d(TAG, "MIGRATION_5_6: Created currencies table")

                    // 2. Insert USD as system currency (cannot be deleted) and EUR as regular currency
                    database.execSQL("""
                        INSERT OR IGNORE INTO currencies
                        (code, name, symbol, exchange_rate_to_default, is_default, is_system_currency, is_manual_rate, created_at, updated_at, last_rate_update)
                        VALUES ('USD', 'US Dollar', '$', '1.0', 1, 1, 0, $timestamp, $timestamp, $timestamp)
                    """)
                    database.execSQL("""
                        INSERT OR IGNORE INTO currencies
                        (code, name, symbol, exchange_rate_to_default, is_default, is_system_currency, is_manual_rate, created_at, updated_at, last_rate_update)
                        VALUES ('EUR', 'Euro', '€', '0.92', 0, 0, 0, $timestamp, $timestamp, $timestamp)
                    """)
                    Log.d(TAG, "MIGRATION_5_6: Inserted USD (system) and EUR (regular)")

                    // 3. Add currency columns to transactions table with default values
                    database.execSQL("ALTER TABLE transactions ADD COLUMN original_currency TEXT NOT NULL DEFAULT 'USD'")
                    database.execSQL("ALTER TABLE transactions ADD COLUMN default_currency TEXT NOT NULL DEFAULT 'USD'")
                    database.execSQL("ALTER TABLE transactions ADD COLUMN amount_in_default_currency TEXT NOT NULL DEFAULT '0.0'")
                    database.execSQL("ALTER TABLE transactions ADD COLUMN exchange_rate TEXT NOT NULL DEFAULT '1.0'")
                    Log.d(TAG, "MIGRATION_5_6: Added currency columns to transactions table")

                    // 4. Populate transaction currency fields based on wallet currency
                    // For existing transactions, we need to:
                    // - Set original_currency from the wallet's currency
                    // - Set default_currency to 'USD' (the default)
                    // - Copy amount to amount_in_default_currency (assuming all were in USD)
                    // - Set exchange_rate to 1.0 (assuming all were in USD)
                    database.execSQL("""
                        UPDATE transactions
                        SET original_currency = COALESCE(
                            (SELECT currency FROM wallets WHERE wallets.id = transactions.fromWalletId),
                            'USD'
                        ),
                        default_currency = 'USD',
                        amount_in_default_currency = amount,
                        exchange_rate = '1.0'
                        WHERE fromWalletId IS NOT NULL
                    """)
                    Log.d(TAG, "MIGRATION_5_6: Updated existing transactions with currency data")

                    // Count migrations
                    val cursor = database.query("SELECT COUNT(*) FROM currencies")
                    cursor.moveToFirst()
                    val totalCurrencies = cursor.getInt(0)
                    cursor.close()

                    Log.i(TAG, "MIGRATION_5_6 completed successfully - Total currencies: $totalCurrencies")
                } catch (e: Exception) {
                    Log.e(TAG, "MIGRATION_5_6 FAILED: ${e.message}", e)
                    throw e
                }
            }
        }
    }
}
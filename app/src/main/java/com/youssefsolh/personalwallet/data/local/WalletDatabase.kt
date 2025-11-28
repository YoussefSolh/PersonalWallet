package com.youssefsolh.personalwallet.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.youssefsolh.personalwallet.data.local.dao.CategoryDao
import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.data.local.dao.WalletDao
import com.youssefsolh.personalwallet.data.local.entity.CategoryEntity
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
        CategoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "wallet_database"

        fun buildDatabase(context: Context): WalletDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                WalletDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Insert default categories on database creation
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = Room.databaseBuilder(
                                context.applicationContext,
                                WalletDatabase::class.java,
                                DATABASE_NAME
                            ).build()

                            val categoryDao = database.categoryDao()
                            DefaultCategories.getDefaultCategories().forEach { category ->
                                categoryDao.insertCategory(
                                    CategoryEntity(
                                        id = category.id,
                                        userId = "default",  // Default categories for all users
                                        name = category.name,
                                        icon = category.icon,
                                        color = category.color,
                                        type = category.type.name,
                                        isDefault = category.isDefault,
                                        isCustom = category.isCustom,
                                        createdAt = category.createdAt,
                                        updatedAt = category.updatedAt,
                                        isDeleted = category.isDeleted
                                    )
                                )
                            }
                        }
                    }
                })
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add userId column to wallets table with ORPHANED marker
                database.execSQL("ALTER TABLE wallets ADD COLUMN userId TEXT NOT NULL DEFAULT 'ORPHANED_DATA'")

                // Add userId column to transactions table with ORPHANED marker
                database.execSQL("ALTER TABLE transactions ADD COLUMN userId TEXT NOT NULL DEFAULT 'ORPHANED_DATA'")

                // Add userId column to categories table with ORPHANED marker (except defaults)
                database.execSQL("ALTER TABLE categories ADD COLUMN userId TEXT NOT NULL DEFAULT 'ORPHANED_DATA'")

                // Update default categories to use 'default' userId
                database.execSQL("UPDATE categories SET userId = 'default' WHERE isDefault = 1")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Insert new default income categories
                database.execSQL("""
                    INSERT OR IGNORE INTO categories
                    (id, userId, name, icon, color, type, isDefault, isCustom, createdAt, updatedAt, isDeleted)
                    VALUES
                    ('income_rental', 'default', 'Rental Income', '🏠', '#795548', 'INCOME', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)
                """)

                database.execSQL("""
                    INSERT OR IGNORE INTO categories
                    (id, userId, name, icon, color, type, isDefault, isCustom, createdAt, updatedAt, isDeleted)
                    VALUES
                    ('income_refund', 'default', 'Refund', '↩️', '#607D8B', 'INCOME', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)
                """)

                // Insert new default expense categories
                val newExpenseCategories = listOf(
                    "('expense_rent', 'default', 'Rent/Mortgage', '🏠', '#795548', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_insurance', 'default', 'Insurance', '🛡️', '#1565C0', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_personal_care', 'default', 'Personal Care', '💇', '#EC407A', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_clothing', 'default', 'Clothing', '👔', '#AB47BC', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_fuel', 'default', 'Gas & Fuel', '⛽', '#EF6C00', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_internet', 'default', 'Internet & Phone', '📡', '#0097A7', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_coffee', 'default', 'Coffee & Snacks', '☕', '#6D4C41', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_gifts', 'default', 'Gifts Given', '🎁', '#D81B60', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_charity', 'default', 'Charity & Donations', '❤️', '#C62828', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_car_maintenance', 'default', 'Car Maintenance', '🔧', '#455A64', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_restaurants', 'default', 'Restaurants & Bars', '🍷', '#6A1B9A', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_loans', 'default', 'Loans & Debt', '🏦', '#424242', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_savings', 'default', 'Savings', '🐷', '#F48FB1', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_children', 'default', 'Children & Childcare', '👶', '#FFB74D', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_business', 'default', 'Business Expenses', '💼', '#5D4037', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_parking', 'default', 'Parking & Tolls', '🅿️', '#78909C', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_laundry', 'default', 'Laundry & Cleaning', '🧺', '#64B5F6', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_books', 'default', 'Books & Media', '📖', '#9575CD', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_furniture', 'default', 'Furniture & Appliances', '🛋️', '#A1887F', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)",
                    "('expense_taxes', 'default', 'Taxes', '📋', '#757575', 'EXPENSE', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)"
                )

                newExpenseCategories.forEach { values ->
                    database.execSQL("""
                        INSERT OR IGNORE INTO categories
                        (id, userId, name, icon, color, type, isDefault, isCustom, createdAt, updatedAt, isDeleted)
                        VALUES $values
                    """)
                }
            }
        }
    }
}
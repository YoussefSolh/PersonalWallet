package com.youssefsolh.personalwallet.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.youssefsolh.personalwallet.domain.model.DefaultCategories
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Comprehensive migration tests for WalletDatabase
 * Tests category insertion and data integrity across migrations
 */
@RunWith(AndroidJUnit4::class)
class WalletDatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        WalletDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        // Clean up the test database
        context.deleteDatabase(TEST_DB)
    }

    @Test
    @Throws(IOException::class)
    fun testMigration4To5_insertsDefaultCategories() {
        // Create database at version 4
        helper.createDatabase(TEST_DB, 4).apply {
            // Create the categories table (simulating existing v4 database)
            execSQL("""
                CREATE TABLE IF NOT EXISTS categories (
                    id TEXT PRIMARY KEY NOT NULL,
                    userId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    icon TEXT NOT NULL,
                    color TEXT NOT NULL,
                    type TEXT NOT NULL,
                    isDefault INTEGER NOT NULL DEFAULT 0,
                    isCustom INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL DEFAULT 0
                )
            """)
            close()
        }

        // Run the migration to version 5
        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true,
            WalletDatabase.MIGRATION_4_5)

        // Query categories from the database
        val cursor = db.query("SELECT * FROM categories WHERE userId = 'default' AND isDeleted = 0")

        val categoriesCount = cursor.count
        val expectedCount = DefaultCategories.getDefaultCategories().size

        // Verify all default categories were inserted
        assertEquals("Should have $expectedCount default categories", expectedCount, categoriesCount)

        // Verify category structure and data
        val categoryIds = mutableListOf<String>()
        while (cursor.moveToNext()) {
            val idIndex = cursor.getColumnIndex("id")
            val userIdIndex = cursor.getColumnIndex("userId")
            val nameIndex = cursor.getColumnIndex("name")
            val iconIndex = cursor.getColumnIndex("icon")
            val colorIndex = cursor.getColumnIndex("color")
            val typeIndex = cursor.getColumnIndex("type")
            val isDefaultIndex = cursor.getColumnIndex("isDefault")
            val isDeletedIndex = cursor.getColumnIndex("isDeleted")

            val id = cursor.getString(idIndex)
            val userId = cursor.getString(userIdIndex)
            val name = cursor.getString(nameIndex)
            val icon = cursor.getString(iconIndex)
            val color = cursor.getString(colorIndex)
            val type = cursor.getString(typeIndex)
            val isDefault = cursor.getInt(isDefaultIndex)
            val isDeleted = cursor.getInt(isDeletedIndex)

            // Verify each category has correct structure
            assertNotNull("Category ID should not be null", id)
            assertEquals("Default categories should have userId = 'default'", "default", userId)
            assertNotNull("Category name should not be null", name)
            assertNotNull("Category icon should not be null", icon)
            assertNotNull("Category color should not be null", color)
            assertTrue("Category type should be INCOME or EXPENSE",
                type == "INCOME" || type == "EXPENSE")
            assertEquals("Should be marked as default", 1, isDefault)
            assertEquals("Should not be deleted", 0, isDeleted)

            categoryIds.add(id)
        }
        cursor.close()

        // Verify specific categories exist
        assertTrue("Should have income_salary category", categoryIds.contains("income_salary"))
        assertTrue("Should have expense_rent category", categoryIds.contains("expense_rent"))
        assertTrue("Should have expense_food category", categoryIds.contains("expense_food"))

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun testMigration4To5_doesNotDuplicateExistingCategories() {
        // Create database at version 4 with some existing categories
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL("""
                CREATE TABLE IF NOT EXISTS categories (
                    id TEXT PRIMARY KEY NOT NULL,
                    userId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    icon TEXT NOT NULL,
                    color TEXT NOT NULL,
                    type TEXT NOT NULL,
                    isDefault INTEGER NOT NULL DEFAULT 0,
                    isCustom INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL DEFAULT 0
                )
            """)

            // Insert a few existing categories
            val timestamp = System.currentTimeMillis()
            execSQL("""
                INSERT INTO categories
                (id, userId, name, icon, color, type, isDefault, isCustom, createdAt, updatedAt, isDeleted)
                VALUES
                ('income_salary', 'default', 'Salary', '💼', '#4CAF50', 'INCOME', 1, 0, $timestamp, $timestamp, 0),
                ('expense_rent', 'default', 'Rent/Mortgage', '🏠', '#795548', 'EXPENSE', 1, 0, $timestamp, $timestamp, 0)
            """)
            close()
        }

        // Run the migration to version 5
        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true,
            WalletDatabase.MIGRATION_4_5)

        // Query categories
        val cursor = db.query("SELECT COUNT(*) FROM categories WHERE userId = 'default' AND isDeleted = 0")
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        val expectedCount = DefaultCategories.getDefaultCategories().size

        // Should have all categories, no duplicates due to INSERT OR REPLACE
        assertEquals("Should have all default categories without duplicates", expectedCount, count)

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun testMigration4To5_categoriesAreNotDeleted() {
        // Create database at version 4
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL("""
                CREATE TABLE IF NOT EXISTS categories (
                    id TEXT PRIMARY KEY NOT NULL,
                    userId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    icon TEXT NOT NULL,
                    color TEXT NOT NULL,
                    type TEXT NOT NULL,
                    isDefault INTEGER NOT NULL DEFAULT 0,
                    isCustom INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL DEFAULT 0
                )
            """)
            close()
        }

        // Run the migration
        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true,
            WalletDatabase.MIGRATION_4_5)

        // Query for deleted categories
        val cursor = db.query("SELECT COUNT(*) FROM categories WHERE userId = 'default' AND isDeleted = 1")
        cursor.moveToFirst()
        val deletedCount = cursor.getInt(0)
        cursor.close()

        assertEquals("No categories should be marked as deleted", 0, deletedCount)

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun testMigration4To5_bothIncomeAndExpenseCategoriesExist() {
        // Create database at version 4
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL("""
                CREATE TABLE IF NOT EXISTS categories (
                    id TEXT PRIMARY KEY NOT NULL,
                    userId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    icon TEXT NOT NULL,
                    color TEXT NOT NULL,
                    type TEXT NOT NULL,
                    isDefault INTEGER NOT NULL DEFAULT 0,
                    isCustom INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL DEFAULT 0
                )
            """)
            close()
        }

        // Run the migration
        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true,
            WalletDatabase.MIGRATION_4_5)

        // Query income categories
        val incomeCursor = db.query(
            "SELECT COUNT(*) FROM categories WHERE userId = 'default' AND type = 'INCOME' AND isDeleted = 0"
        )
        incomeCursor.moveToFirst()
        val incomeCount = incomeCursor.getInt(0)
        incomeCursor.close()

        // Query expense categories
        val expenseCursor = db.query(
            "SELECT COUNT(*) FROM categories WHERE userId = 'default' AND type = 'EXPENSE' AND isDeleted = 0"
        )
        expenseCursor.moveToFirst()
        val expenseCount = expenseCursor.getInt(0)
        expenseCursor.close()

        // Count from DefaultCategories
        val defaultCategories = DefaultCategories.getDefaultCategories()
        val expectedIncomeCount = defaultCategories.count { it.type.name == "INCOME" }
        val expectedExpenseCount = defaultCategories.count { it.type.name == "EXPENSE" }

        assertEquals("Should have $expectedIncomeCount income categories", expectedIncomeCount, incomeCount)
        assertEquals("Should have $expectedExpenseCount expense categories", expectedExpenseCount, expenseCount)
        assertTrue("Should have both income and expense categories", incomeCount > 0 && expenseCount > 0)

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun testNewDatabaseCreation_hasDefaultCategories() {
        // Create a new database using buildDatabase method
        val database = WalletDatabase.buildDatabase(context)

        // Wait a bit for onCreate callback to complete
        Thread.sleep(500)

        // Query categories
        val categoryDao = database.categoryDao()
        val categoriesFlow = categoryDao.getAllCategories("test_user")

        // Since it returns a Flow, we need to collect it
        // For testing, we can query directly
        val cursor = database.openHelper.readableDatabase.query(
            "SELECT COUNT(*) FROM categories WHERE userId = 'default' AND isDeleted = 0"
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        val expectedCount = DefaultCategories.getDefaultCategories().size
        assertEquals("New database should have all default categories", expectedCount, count)

        database.close()
    }

    @Test
    @Throws(IOException::class)
    fun testMigration4To5_categoryFieldsAreCorrect() {
        // Create database at version 4
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL("""
                CREATE TABLE IF NOT EXISTS categories (
                    id TEXT PRIMARY KEY NOT NULL,
                    userId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    icon TEXT NOT NULL,
                    color TEXT NOT NULL,
                    type TEXT NOT NULL,
                    isDefault INTEGER NOT NULL DEFAULT 0,
                    isCustom INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL DEFAULT 0
                )
            """)
            close()
        }

        // Run the migration
        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true,
            WalletDatabase.MIGRATION_4_5)

        // Get a specific category and verify its fields
        val cursor = db.query("SELECT * FROM categories WHERE id = 'income_salary' LIMIT 1")
        assertTrue("Should find income_salary category", cursor.moveToFirst())

        val name = cursor.getString(cursor.getColumnIndex("name"))
        val icon = cursor.getString(cursor.getColumnIndex("icon"))
        val color = cursor.getString(cursor.getColumnIndex("color"))
        val type = cursor.getString(cursor.getColumnIndex("type"))
        val userId = cursor.getString(cursor.getColumnIndex("userId"))
        val isDefault = cursor.getInt(cursor.getColumnIndex("isDefault"))
        val isCustom = cursor.getInt(cursor.getColumnIndex("isCustom"))
        val isDeleted = cursor.getInt(cursor.getColumnIndex("isDeleted"))

        assertEquals("Name should be Salary", "Salary", name)
        assertEquals("Icon should be 💼", "💼", icon)
        assertEquals("Color should be #4CAF50", "#4CAF50", color)
        assertEquals("Type should be INCOME", "INCOME", type)
        assertEquals("UserId should be default", "default", userId)
        assertEquals("isDefault should be 1", 1, isDefault)
        assertEquals("isCustom should be 0", 0, isCustom)
        assertEquals("isDeleted should be 0", 0, isDeleted)

        cursor.close()
        db.close()
    }
}

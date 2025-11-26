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
    version = 1,
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
                .addMigrations()
                .build()
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }
    }
}
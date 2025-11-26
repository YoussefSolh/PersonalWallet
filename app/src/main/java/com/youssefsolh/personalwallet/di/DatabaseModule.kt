package com.youssefsolh.personalwallet.di

import android.content.Context
import androidx.room.Room
import com.youssefsolh.personalwallet.data.local.WalletDatabase
import com.youssefsolh.personalwallet.data.local.dao.CategoryDao
import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.data.local.dao.WalletDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWalletDatabase(@ApplicationContext context: Context): WalletDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            WalletDatabase::class.java,
            WalletDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideWalletDao(database: WalletDatabase): WalletDao = database.walletDao()

    @Provides
    fun provideTransactionDao(database: WalletDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideCategoryDao(database: WalletDatabase): CategoryDao = database.categoryDao()
}
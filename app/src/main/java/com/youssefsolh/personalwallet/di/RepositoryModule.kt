package com.youssefsolh.personalwallet.di

import com.youssefsolh.personalwallet.data.repository.CategoryRepositoryImpl
import com.youssefsolh.personalwallet.data.repository.CurrencyRepositoryImpl
import com.youssefsolh.personalwallet.data.repository.TransactionRepositoryImpl
import com.youssefsolh.personalwallet.data.repository.WalletRepositoryImpl
import com.youssefsolh.personalwallet.domain.repository.CategoryRepository
import com.youssefsolh.personalwallet.domain.repository.CurrencyRepository
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepository(
        walletRepositoryImpl: WalletRepositoryImpl
    ): WalletRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindCurrencyRepository(
        currencyRepositoryImpl: CurrencyRepositoryImpl
    ): CurrencyRepository
}
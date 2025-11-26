package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class CreateWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(name: String, initialBalance: BigDecimal, currency: String) {
        val wallet = Wallet(
            id = UUID.randomUUID().toString(),
            name = name,
            balance = initialBalance,
            currency = currency,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        walletRepository.insertWallet(wallet)
    }
}

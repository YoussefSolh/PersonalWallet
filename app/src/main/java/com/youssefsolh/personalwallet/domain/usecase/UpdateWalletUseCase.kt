package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import javax.inject.Inject

/**
 * Use case for updating an existing wallet
 */
class UpdateWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(wallet: Wallet) {
        walletRepository.updateWallet(wallet)
    }
}

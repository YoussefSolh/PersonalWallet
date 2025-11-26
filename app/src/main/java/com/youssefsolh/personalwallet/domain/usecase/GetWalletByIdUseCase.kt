package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import javax.inject.Inject

/**
 * Use case for getting a wallet by its ID
 */
class GetWalletByIdUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(id: String): Wallet? {
        return walletRepository.getWalletById(id)
    }
}

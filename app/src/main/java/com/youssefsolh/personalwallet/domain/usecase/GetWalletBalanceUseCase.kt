package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject

class GetWalletBalanceUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(walletId: String): Flow<BigDecimal> {
        return walletRepository.getWalletBalance(walletId)
    }
}
package com.youssefsolh.personalwallet.domain.model

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Wallet(
    val id: String,
    val name: String,
    val balance: @Serializable(with = BigDecimalSerializer::class) BigDecimal,
    val currency: String = "USD",
    val currencySymbol: String = "$",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    /**
     * Format balance with currency symbol and thousand separators.
     * Example: $1,234,456.08
     */
    fun getFormattedBalance(): String {
        return formatAmountWithCurrency(balance, currencySymbol)
    }

    companion object {
        /**
         * Format an amount with currency symbol and thousand separators.
         * @param amount The amount to format
         * @param symbol The currency symbol to use
         * @return Formatted string like "$1,234,456.08"
         */
        fun formatAmountWithCurrency(amount: BigDecimal, symbol: String): String {
            val formatter = java.text.DecimalFormat("#,##0.00")
            return "$symbol${formatter.format(amount)}"
        }
    }
}
package com.youssefsolh.personalwallet.domain.model

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * A currency a wallet can be denominated in.
 *
 * [exchangeRateToDefault] is how many units of this currency equal one unit of the
 * default currency (e.g. EUR = 0.92 when USD is the default).
 */
@Serializable
data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val exchangeRateToDefault: @Serializable(with = BigDecimalSerializer::class) BigDecimal = BigDecimal.ONE,
    val isDefault: Boolean = false,
    val isSystemCurrency: Boolean = false,
    val isManualRate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastRateUpdate: Long? = null
) {
    /** Converts [amount] expressed in this currency into the default currency. */
    fun toDefault(amount: BigDecimal): BigDecimal =
        amount.divide(exchangeRateToDefault, 10, RoundingMode.HALF_UP)

    /** Converts [amount] expressed in the default currency into this currency. */
    fun fromDefault(amount: BigDecimal): BigDecimal =
        amount.multiply(exchangeRateToDefault)
}

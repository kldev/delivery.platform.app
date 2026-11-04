package com.pl.platform.reporting.common

import com.pl.platform.reporting.model.TotalWithCurrency
import java.math.BigDecimal

object CurrencyTotalCalculator {

    fun <T> sumByCurrency(
        items: Iterable<T>,
        amount: (T) -> BigDecimal,
        currency: (T) -> String
    ): List<TotalWithCurrency> =
        items
            .groupBy(currency)
            .map { (currency, values) ->
                TotalWithCurrency(
                    total = values.sumOf(amount),
                    currency = currency
                )
            }
            .sortedBy { it.currency }
}
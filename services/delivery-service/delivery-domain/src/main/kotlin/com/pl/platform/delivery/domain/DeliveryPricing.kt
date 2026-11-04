package com.pl.platform.delivery.domain

import java.math.BigDecimal

/** Price of a delivery by distance. Pure domain rule, no state. */
object DeliveryPricing {
    fun calculate(distanceKm: BigDecimal): BigDecimal {
        require(distanceKm > BigDecimal.ZERO) {
            "Distance must be greater than zero."
        }

        return when {
            distanceKm <= BigDecimal("10") -> BigDecimal("5.00")
            distanceKm <= BigDecimal("50") -> BigDecimal("10.00")
            distanceKm <= BigDecimal("100") -> BigDecimal("20.00")
            else -> BigDecimal("50.00")
        }
    }
}

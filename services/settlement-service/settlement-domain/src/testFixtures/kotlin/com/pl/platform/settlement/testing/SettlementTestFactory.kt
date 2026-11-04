package com.pl.platform.settlement.testing

import com.pl.platform.settlement.domain.Settlement
import com.pl.platform.settlement.domain.SettlementId
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

object SettlementTestFactory {

    fun create(
        id: UUID = UUID.randomUUID(),
        deliveryId: UUID = UUID.randomUUID(),
        driverId: UUID = UUID.randomUUID(),
        driverFullName: String = "John Connor",
        deliveryAmount: BigDecimal = BigDecimal("100.00"),
        currency: String = "EUR",
        basePercentage: BigDecimal = BigDecimal("70.00"),
        nightPercentage: BigDecimal = BigDecimal.ZERO,
        weekendPercentage: BigDecimal = BigDecimal.ZERO,
        distancePercentage: BigDecimal = BigDecimal.ZERO,
        totalPercentage: BigDecimal = BigDecimal("70.00"),
        driverAmount: BigDecimal = BigDecimal("70.00"),
        distanceKm: BigDecimal = BigDecimal("50.00"),
        completedAt: Instant = Instant.now(),
    ): Settlement =
        Settlement(
            id = SettlementId(id),
            deliveryId = deliveryId,
            driverId = driverId,
            driverFullName = driverFullName,
            deliveryAmount = deliveryAmount,
            currency = currency,
            basePercentage = basePercentage,
            nightPercentage = nightPercentage,
            weekendPercentage = weekendPercentage,
            distancePercentage = distancePercentage,
            totalPercentage = totalPercentage,
            driverAmount = driverAmount,
            distanceKm = distanceKm,
            completedAt = completedAt,
            createdAt = Instant.now(),
        )
}
package com.pl.platform.settlement.domain

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/** The driver's share of one completed delivery; created once, never changed. */
class Settlement(
    val id: SettlementId,
    val deliveryId: UUID,
    val driverId: UUID,
    val driverFullName: String,
    val deliveryAmount: BigDecimal,
    val currency: String,
    val basePercentage: BigDecimal,
    val nightPercentage: BigDecimal,
    val weekendPercentage: BigDecimal,
    val distancePercentage: BigDecimal,
    val totalPercentage: BigDecimal,
    val driverAmount: BigDecimal,
    val distanceKm: BigDecimal,
    val completedAt: Instant,
    val createdAt: Instant,
) {
    companion object {

        fun create(
            deliveryId: UUID,
            driverId: UUID,
            driverFullName: String,
            deliveryAmount: BigDecimal,
            currency: String,
            basePercentage: BigDecimal,
            nightPercentage: BigDecimal,
            weekendPercentage: BigDecimal,
            distancePercentage: BigDecimal,
            totalPercentage: BigDecimal,
            driverAmount: BigDecimal,
            distanceKm: BigDecimal,
            completedAt: Instant,
            now: Instant,
        ): Settlement =
            Settlement(
                id = SettlementId(UUID.randomUUID()),
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
                createdAt = now,
            )
    }
}
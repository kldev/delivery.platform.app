package com.pl.platform.settlement.web.settlements

import com.pl.platform.settlement.application.port.SettlementDetails
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/** Field names are read by reporting-service. Percentages are 0-100. */
data class SettlementResponse(
    val id: UUID,
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
        fun from(details: SettlementDetails) =
            SettlementResponse(
                id = details.id,
                deliveryId = details.deliveryId,
                driverId = details.driverId,
                driverFullName = details.driverFullName,
                deliveryAmount = details.deliveryAmount,
                currency = details.currency,
                basePercentage = details.basePercentage,
                nightPercentage = details.nightPercentage,
                weekendPercentage = details.weekendPercentage,
                distancePercentage = details.distancePercentage,
                totalPercentage = details.totalPercentage,
                driverAmount = details.driverAmount,
                distanceKm = details.distanceKm,
                completedAt = details.completedAt,
                createdAt = details.createdAt,
            )
    }
}

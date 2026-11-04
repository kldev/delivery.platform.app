package com.pl.platform.settlement.application.port

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/** Read model shared by getsettlement and listsettlements. */
data class SettlementDetails(
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
)

package com.pl.platform.svc.settlement.client.model

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

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
)
package com.pl.platform.settlement.application.deliverycompleted

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/** From DeliveryCompletedEvent; [completedAt] is the event's occurredAt (the bonuses depend on it). */
data class DeliveryCompletedCommand(
    val eventId: UUID,
    val eventType: String,
    val deliveryId: UUID,
    val driverId: UUID,
    val driverFullName: String,
    val deliveryAmount: BigDecimal,
    val currency: String,
    val distanceKm: BigDecimal,
    val completedAt: Instant,
)

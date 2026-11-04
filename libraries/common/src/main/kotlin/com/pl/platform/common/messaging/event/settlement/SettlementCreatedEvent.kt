package com.pl.platform.common.messaging.event.settlement

import com.pl.platform.common.messaging.event.Event
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class SettlementCreatedEvent(
    override val settlementId: UUID,
    val deliveryId: UUID,
    val driverId: UUID,
    val driverFullName: String = "",
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
    // In the constructor (not only the base class defaults) so that deserializing keeps the producer's values.
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
) : SettlementEvent(
    settlementId,
    aggregateId = settlementId,
    eventType = SettlementEventType.Created.value,
    eventId = eventId,
    occurredAt = occurredAt,
)


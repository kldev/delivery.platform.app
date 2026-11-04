package com.pl.platform.common.messaging.event.settlement

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class DriverSettlementCompletedEvent(
    override val settlementId: UUID,
    val driverId: UUID,
    val amount: BigDecimal,
    val currency: String,
    val driverFullName: String,
    val deliveryId: UUID,
    // In the constructor (not only the base class defaults) so that deserializing keeps the producer's values.
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
) : SettlementEvent(
    settlementId = settlementId,
    eventType = SettlementEventType.DriverSettlementCompleted.value,
    eventId = eventId,
    occurredAt = occurredAt,
)
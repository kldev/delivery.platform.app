package com.pl.platform.common.messaging.event.delivery

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class DeliveryCompletedEvent(
    override val deliveryId: UUID,
    val driverId: UUID,
    val driverFullName: String,
    val distanceKm: BigDecimal,
    val price: BigDecimal,
    val currency: String = "PLN",
    // In the constructor (not only the base class defaults) so that deserializing keeps the producer's values.
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
) : DeliveryEvent(deliveryId = deliveryId,
    eventType = DeliveryEventType.COMPLETED.value,
    eventId = eventId,
    occurredAt = occurredAt,
)
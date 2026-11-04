package com.pl.platform.common.messaging.event.delivery

import java.time.Instant
import java.util.UUID

data class DeliveryAssignedEvent(
    override val deliveryId: UUID,
    val driverId: UUID,
    // In the constructor (not only the base class defaults) so that deserializing keeps the producer's values.
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
) : DeliveryEvent(
    deliveryId,
    eventType = DeliveryEventType.ASSIGNED.value,
    eventId = eventId,
    occurredAt = occurredAt,
)
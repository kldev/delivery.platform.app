package com.pl.platform.common.messaging.event.delivery

import java.time.Instant
import java.util.UUID

data class DeliveryStartedEvent(
    override val deliveryId: UUID,
    // In the constructor (not only the base class defaults) so that deserializing keeps the producer's values.
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
) : DeliveryEvent(
    deliveryId = deliveryId,
    eventType = DeliveryEventType.STARTED.value,
    eventId = eventId,
    occurredAt = occurredAt,
)
package com.pl.platform.common.messaging.event.delivery

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class DeliveryConfirmedEvent(
    override val deliveryId: UUID,
    var price: BigDecimal,
    var currency: String = "PLN",
    // In the constructor (not only the base class defaults) so that deserializing keeps the producer's values.
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
) : DeliveryEvent(deliveryId = deliveryId,
    eventType = DeliveryEventType.CONFIRMED.value,
    eventId = eventId,
    occurredAt = occurredAt,
)
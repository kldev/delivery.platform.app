package com.pl.platform.common.messaging.event.payments

import com.pl.platform.common.messaging.event.Event
import java.time.Instant
import java.util.UUID

data class PaymentPaidEvent(
    override val deliveryId: UUID,
    override val paymentId: UUID,
    // In the constructor (not only the base class defaults) so that deserializing keeps the producer's values.
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
) : PaymentEvent(
    paymentId = paymentId,
    deliveryId= deliveryId,
    eventType = PaymentEventType.Paid.value,
    eventId = eventId,
    occurredAt = occurredAt,
)
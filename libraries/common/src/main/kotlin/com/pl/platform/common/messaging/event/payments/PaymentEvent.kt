package com.pl.platform.common.messaging.event.payments

import com.pl.platform.common.messaging.event.Event
import java.time.Instant
import java.util.UUID

abstract class PaymentEvent(
    open val paymentId: UUID,
    open val deliveryId: UUID,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val aggregateId: UUID = paymentId,
    override val module: String = "payment",
    override val eventType: String
) : Event {
}
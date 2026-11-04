package com.pl.platform.common.messaging.event.payments

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PaymentCompletedEvent(
    override val paymentId: UUID,
    override val deliveryId: UUID,
    val externalTransactionId: String,
    val amount: BigDecimal,
    val currency: String,
    // In the constructor (not only the base class defaults) so that deserializing keeps the producer's values.
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
) : PaymentEvent(
        paymentId = paymentId,
        deliveryId = deliveryId,
        eventType = PaymentEventType.Completed.value,
    eventId = eventId,
    occurredAt = occurredAt,
){
}
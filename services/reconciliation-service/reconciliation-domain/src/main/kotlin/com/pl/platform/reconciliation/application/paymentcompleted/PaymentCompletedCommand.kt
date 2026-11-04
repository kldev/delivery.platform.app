package com.pl.platform.reconciliation.application.paymentcompleted

import java.math.BigDecimal
import java.util.UUID

/** From PaymentCompletedEvent: the amount actually transferred, with the provider's transaction id. */
data class PaymentCompletedCommand(
    val eventId: UUID,
    val eventType: String,
    val paymentId: UUID,
    val deliveryId: UUID,
    val externalTransactionId: String,
    val amount: BigDecimal,
    val currency: String,
)

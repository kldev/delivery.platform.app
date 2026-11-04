package com.pl.platform.delivery.application.paymentdeclined

import com.pl.platform.delivery.domain.DeliveryId
import java.util.UUID

data class PaymentDeclinedCommand(
    val eventId: UUID,
    val eventType: String,
    val deliveryId: DeliveryId,
)

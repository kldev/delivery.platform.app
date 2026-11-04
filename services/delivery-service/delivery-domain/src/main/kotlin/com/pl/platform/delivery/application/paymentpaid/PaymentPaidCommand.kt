package com.pl.platform.delivery.application.paymentpaid

import com.pl.platform.delivery.domain.DeliveryId
import java.util.UUID

data class PaymentPaidCommand(
    val eventId: UUID,
    val eventType: String,
    val deliveryId: DeliveryId,
)

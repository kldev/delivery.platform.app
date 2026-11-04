package com.pl.platform.delivery.application.complete

import com.pl.platform.delivery.domain.DeliveryId

data class CompleteDeliveryCommand(
    val deliveryId: DeliveryId,
)

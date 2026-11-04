package com.pl.platform.delivery.application.cancel

import com.pl.platform.delivery.domain.DeliveryId

data class CancelDeliveryCommand(
    val deliveryId: DeliveryId,
)

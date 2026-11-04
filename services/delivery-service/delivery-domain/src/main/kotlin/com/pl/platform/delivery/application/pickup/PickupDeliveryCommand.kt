package com.pl.platform.delivery.application.pickup

import com.pl.platform.delivery.domain.DeliveryId

data class PickupDeliveryCommand(
    val deliveryId: DeliveryId,
)

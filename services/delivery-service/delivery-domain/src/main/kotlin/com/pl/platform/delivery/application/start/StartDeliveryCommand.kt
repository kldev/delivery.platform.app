package com.pl.platform.delivery.application.start

import com.pl.platform.delivery.domain.DeliveryId

data class StartDeliveryCommand(
    val deliveryId: DeliveryId,
)

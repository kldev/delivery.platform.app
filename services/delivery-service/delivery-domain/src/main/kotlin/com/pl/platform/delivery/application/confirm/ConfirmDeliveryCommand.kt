package com.pl.platform.delivery.application.confirm

import com.pl.platform.delivery.domain.DeliveryId

data class ConfirmDeliveryCommand(
    val deliveryId: DeliveryId,
)

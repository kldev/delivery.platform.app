package com.pl.platform.delivery.application.create

import com.pl.platform.delivery.domain.DeliveryId
import com.pl.platform.delivery.domain.DeliveryStatus
import java.math.BigDecimal

data class CreateDeliveryCommand(
    val pickupAddress: String,
    val deliveryAddress: String,
    val distanceKm: BigDecimal = BigDecimal.ZERO,
    val currency: String = "PLN",
)

data class CreateDeliveryResult(
    val deliveryId: DeliveryId,
    val price: BigDecimal,
    val currency: String,
    val status: DeliveryStatus,
)

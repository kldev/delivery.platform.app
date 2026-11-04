package com.pl.platform.svc.delivery.client.model

import java.math.BigDecimal
import java.util.UUID

data class DeliveryItemResponse(
    val id: UUID,
    val driverId: UUID?,
    val pickupAddress: String,
    val deliveryAddress: String,
    val price: BigDecimal,
    val currency: String = "PLN",
    val status: DeliveryStatus,
    val distanceKm: BigDecimal)
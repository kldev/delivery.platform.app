package com.pl.platform.delivery.application.port

import com.pl.platform.delivery.domain.DeliveryStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/** Read model of a delivery, shared by getdelivery and listdeliveries. */
data class DeliveryDetails(
    val id: UUID,
    val driverId: UUID?,
    val pickupAddress: String,
    val deliveryAddress: String,
    val price: BigDecimal,
    val status: DeliveryStatus,
    val distanceKm: BigDecimal,
    val currency: String,
    val createdAt: Instant,
    val updatedAt: Instant?,
)

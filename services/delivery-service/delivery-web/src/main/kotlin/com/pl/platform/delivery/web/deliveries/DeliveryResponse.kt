package com.pl.platform.delivery.web.deliveries

import com.pl.platform.delivery.application.port.DeliveryDetails
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/** A delivery, in GET /api/deliveries/{id} and in the list. Field names are read by reporting and notification. */
data class DeliveryResponse(
    val id: UUID,
    val driverId: UUID?,
    val pickupAddress: String,
    val deliveryAddress: String,
    val price: BigDecimal,
    val status: DeliveryStatusDto,
    val distanceKm: BigDecimal,
    val currency: String,
    val createdAt: Instant,
    val updatedAt: Instant?,
) {
    companion object {
        fun from(details: DeliveryDetails) =
            DeliveryResponse(
                id = details.id,
                driverId = details.driverId,
                pickupAddress = details.pickupAddress,
                deliveryAddress = details.deliveryAddress,
                price = details.price,
                status = DeliveryStatusDto.from(details.status),
                distanceKm = details.distanceKm,
                currency = details.currency,
                createdAt = details.createdAt,
                updatedAt = details.updatedAt,
            )
    }
}

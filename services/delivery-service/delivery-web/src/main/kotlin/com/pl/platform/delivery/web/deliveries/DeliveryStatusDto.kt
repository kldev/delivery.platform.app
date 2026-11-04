package com.pl.platform.delivery.web.deliveries

import com.pl.platform.delivery.domain.DeliveryStatus

/** Delivery status as exposed by the API; renaming a domain status must not silently change the contract. */
enum class DeliveryStatusDto {
    CREATED,
    CONFIRMED,
    ASSIGNED,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED,
    ;

    fun toDomain(): DeliveryStatus =
        when (this) {
            CREATED -> DeliveryStatus.CREATED
            CONFIRMED -> DeliveryStatus.CONFIRMED
            ASSIGNED -> DeliveryStatus.ASSIGNED
            PICKED_UP -> DeliveryStatus.PICKED_UP
            IN_TRANSIT -> DeliveryStatus.IN_TRANSIT
            DELIVERED -> DeliveryStatus.DELIVERED
            CANCELLED -> DeliveryStatus.CANCELLED
        }

    companion object {
        fun from(status: DeliveryStatus): DeliveryStatusDto =
            when (status) {
                DeliveryStatus.CREATED -> CREATED
                DeliveryStatus.CONFIRMED -> CONFIRMED
                DeliveryStatus.ASSIGNED -> ASSIGNED
                DeliveryStatus.PICKED_UP -> PICKED_UP
                DeliveryStatus.IN_TRANSIT -> IN_TRANSIT
                DeliveryStatus.DELIVERED -> DELIVERED
                DeliveryStatus.CANCELLED -> CANCELLED
            }
    }
}

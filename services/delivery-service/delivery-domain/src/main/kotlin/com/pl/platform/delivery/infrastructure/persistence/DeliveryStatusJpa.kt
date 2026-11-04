package com.pl.platform.delivery.infrastructure.persistence

import com.pl.platform.delivery.domain.DeliveryStatus

enum class DeliveryStatusJpa {
    CREATED,
    CONFIRMED,
    ASSIGNED,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED;

    fun toDomain(): DeliveryStatus =
        DeliveryStatus.valueOf(name)

    companion object {
        fun from(status: DeliveryStatus): DeliveryStatusJpa =
            valueOf(status.name)
    }
}
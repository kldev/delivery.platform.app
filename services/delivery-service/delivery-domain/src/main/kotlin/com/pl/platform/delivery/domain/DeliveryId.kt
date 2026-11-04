package com.pl.platform.delivery.domain

import java.util.UUID

@JvmInline
value class DeliveryId(
    val value: UUID
) {
    companion object {
        fun new(): DeliveryId =
            DeliveryId(UUID.randomUUID())
    }
}
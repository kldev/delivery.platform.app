package com.pl.platform.delivery.domain

import java.util.UUID

@JvmInline
value class DriverId(
    val value: UUID
) {
    companion object {
        fun generate(): DriverId =
            DriverId(UUID.randomUUID())
    }
}
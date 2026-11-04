package com.pl.platform.delivery.infrastructure.persistence

import com.pl.platform.delivery.domain.DriverStatus

enum class DriverStatusJpa {
    ACTIVE,
    INACTIVE,
    SUSPENDED;

    fun toDomain(): DriverStatus =
        when (this) {
            ACTIVE -> DriverStatus.ACTIVE
            INACTIVE -> DriverStatus.INACTIVE
            SUSPENDED -> DriverStatus.SUSPENDED
        }

    companion object {

        fun from(status: DriverStatus): DriverStatusJpa =
            when (status) {
                DriverStatus.ACTIVE -> ACTIVE
                DriverStatus.INACTIVE -> INACTIVE
                DriverStatus.SUSPENDED -> SUSPENDED
            }
    }
}
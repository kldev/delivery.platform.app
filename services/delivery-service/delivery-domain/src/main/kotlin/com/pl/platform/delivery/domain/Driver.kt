package com.pl.platform.delivery.domain

import java.time.Instant

class Driver private constructor(
    val id: DriverId,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    status: DriverStatus,
    val createdAt: Instant,
    val email: String
) {

    var status: DriverStatus = status
        private set

    fun activate() {
        check(status != DriverStatus.ACTIVE) {
            "Driver is already active"
        }

        status = DriverStatus.ACTIVE
    }

    fun deactivate() {
        check(status != DriverStatus.INACTIVE) {
            "Driver is already inactive"
        }

        status = DriverStatus.INACTIVE
    }

    fun suspend() {
        check(status != DriverStatus.SUSPENDED) {
            "Driver is already suspended"
        }

        status = DriverStatus.SUSPENDED
    }

    fun fullName() : String = "$firstName $lastName"

    companion object {

        fun create(
            firstName: String,
            lastName: String,
            phoneNumber: String,
            email: String
        ) = Driver(
            id = DriverId.generate(),
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            status = DriverStatus.ACTIVE,
            createdAt = Instant.now(),
            email = email
        )

        fun restore(
            id: DriverId,
            firstName: String,
            lastName: String,
            phoneNumber: String,
            status: DriverStatus,
            createdAt: Instant,
            email: String
        ) = Driver(
            id = id,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            status = status,
            createdAt = createdAt,
            email = email
        )
    }
}
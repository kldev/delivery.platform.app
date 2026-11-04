package com.pl.platform.delivery.application.port

import com.pl.platform.delivery.domain.Driver
import com.pl.platform.delivery.domain.DriverId

interface DriverRepository {
    fun findById(id: DriverId): Driver?

    fun create(driver: Driver)

    fun existsById(id: DriverId): Boolean

    fun existsByPhoneNumber(phoneNumber: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun exists(): Boolean

    /** All drivers, or the one with [driverId] when given. */
    fun findAll(driverId: DriverId? = null): List<Driver>

    /** A random driver, or null when there are none. Picked by the database, without loading the table. */
    fun findRandomId(): DriverId?
}

package com.pl.platform.delivery.application.port

import com.pl.platform.delivery.domain.Driver
import com.pl.platform.delivery.domain.DriverId

/** Read model of a driver, shared by createdriver, getdriver and listdrivers. */
data class DriverDetails(
    val id: DriverId,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
) {
    companion object {
        fun from(driver: Driver) =
            DriverDetails(
                id = driver.id,
                firstName = driver.firstName,
                lastName = driver.lastName,
                phoneNumber = driver.phoneNumber,
                email = driver.email,
            )
    }
}

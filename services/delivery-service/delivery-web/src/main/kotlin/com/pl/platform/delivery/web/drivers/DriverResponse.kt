package com.pl.platform.delivery.web.drivers

import com.pl.platform.delivery.application.port.DriverDetails
import java.util.UUID

data class DriverResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
) {
    companion object {
        fun from(driver: DriverDetails) =
            DriverResponse(
                id = driver.id.value,
                firstName = driver.firstName,
                lastName = driver.lastName,
                phoneNumber = driver.phoneNumber,
                email = driver.email,
            )
    }
}

package com.pl.platform.delivery.application.createdriver

import com.pl.platform.delivery.application.port.DriverDetails
import com.pl.platform.delivery.application.port.DriverRepository
import com.pl.platform.delivery.domain.ConflictException
import com.pl.platform.delivery.domain.Driver
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateDriverHandler(
    private val driverRepository: DriverRepository,
) {
    @Transactional
    fun handle(command: CreateDriverCommand): DriverDetails {
        val driver =
            Driver.create(
                firstName = command.firstName.trim(),
                lastName = command.lastName.trim(),
                phoneNumber = command.phoneNumber.trim(),
                email = command.email.trim().lowercase(),
            )

        if (driverRepository.existsByPhoneNumber(driver.phoneNumber)) {
            throw ConflictException("driver.phone-number-taken", "A driver with this phone number already exists")
        }
        if (driverRepository.existsByEmail(driver.email)) {
            throw ConflictException("driver.email-taken", "A driver with this email already exists")
        }
        driverRepository.create(driver)

        return DriverDetails.from(driver)
    }
}

package com.pl.platform.delivery.infrastructure.seed

import com.pl.platform.delivery.application.port.DriverRepository
import com.pl.platform.delivery.domain.Driver
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Profile("dev")
@Component
class DriverSeeder(private val driverRepository: DriverRepository) {

    @Transactional
    @EventListener(ApplicationReadyEvent::class)
    fun seed() {
        if (driverRepository.exists()) {
            return
        }

        val driverA = Driver.create(firstName = "John" , lastName = "Smith",  phoneNumber = "+1530200111", "j.smith@fake.io")
        val driverB = Driver.create(firstName = "Annie" , lastName = "Stockhouse",  phoneNumber = "+1530200221", "a.stock@fake.io")
        val driverC = Driver.create(firstName = "Chandler" , lastName = "Marius",  phoneNumber = "+15302001121", "c.marius@fake.io")

        driverRepository.create(driverA)
        driverRepository.create(driverB)
        driverRepository.create(driverC)
    }

}
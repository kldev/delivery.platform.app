package com.pl.platform.delivery.testing

import com.pl.platform.delivery.infrastructure.persistence.DriverJpaEntity
import com.pl.platform.delivery.infrastructure.persistence.SpringDataDriverRepository
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

@Component
class DriverDatabaseFixture(
    private val driverRepository: SpringDataDriverRepository,
    private val random: SecureRandom = SecureRandom()
) {

    fun create(
        id: UUID = UUID.randomUUID(),
        firstName: String = "John",
        lastName: String = "Connor",
        phoneNumber: String = "+45" + random.nextInt(1000) + random.nextInt(1000),
    ): UUID {

        val driver = DriverTestFactory.create(
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
        )

        driverRepository.saveAndFlush(DriverJpaEntity.create(driver, Instant.now()))

        return driver.id.value
    }
}
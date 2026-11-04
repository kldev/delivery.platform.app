package com.pl.platform.delivery.infrastructure.persistence

import com.pl.platform.delivery.domain.Driver
import com.pl.platform.delivery.domain.DriverId
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "drivers",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_drivers_phone_number",
            columnNames = ["phone_number"]
        )
    ]
)
class DriverJpaEntity(

    @Id
    var id: UUID,

    @Column(name = "first_name", nullable = false, length = 100)
    var firstName: String,

    @Column(name = "last_name", nullable = false, length = 100)
    var lastName: String,

    @Column(name = "phone_number", nullable = false, length = 30)
    var phoneNumber: String,

    @Column(name = "email", nullable = false, length = 320)
    var email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: DriverStatusJpa,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant
) {

    fun toDomain(): Driver =
        Driver.restore(
            id = DriverId(id),
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            status = status.toDomain(),
            createdAt = createdAt,
            email = email,
        )

    fun updateFrom(
        driver: Driver
    ) {
        firstName = driver.firstName
        lastName = driver.lastName
        phoneNumber = driver.phoneNumber
        status = DriverStatusJpa.from(driver.status)
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }

    companion object {

        fun create(
            driver: Driver,
            now: Instant = Instant.now()
        ): DriverJpaEntity =
            DriverJpaEntity(
                id = driver.id.value,
                firstName = driver.firstName,
                lastName = driver.lastName,
                phoneNumber = driver.phoneNumber,
                status = DriverStatusJpa.from(driver.status),
                createdAt = driver.createdAt,
                updatedAt = now,
                email = driver.email,
            )
    }
}
package com.pl.platform.delivery.infrastructure.persistence

import com.pl.platform.delivery.application.port.DriverRepository
import com.pl.platform.delivery.domain.Driver
import com.pl.platform.delivery.domain.DriverId
import org.springframework.stereotype.Repository

@Repository
class JpaDriverRepository(
    private val repository: SpringDataDriverRepository,
) : DriverRepository {
    override fun findById(id: DriverId): Driver? =
        repository
            .findById(id.value)
            .map(DriverJpaEntity::toDomain)
            .orElse(null)

    override fun create(driver: Driver) {
        repository.save(DriverJpaEntity.create(driver))
    }

    override fun existsById(id: DriverId): Boolean = repository.existsById(id.value)

    override fun existsByPhoneNumber(phoneNumber: String): Boolean = repository.existsByPhoneNumber(phoneNumber)

    override fun existsByEmail(email: String): Boolean = repository.existsByEmail(email)

    override fun exists(): Boolean = repository.count() > 0

    override fun findAll(driverId: DriverId?): List<Driver> {
        val entities =
            if (driverId != null) {
                repository.findAllById(driverId.value)
            } else {
                repository.findAll()
            }
        return entities.map(DriverJpaEntity::toDomain)
    }

    override fun findRandomId(): DriverId? = repository.findRandomId()?.let(::DriverId)
}

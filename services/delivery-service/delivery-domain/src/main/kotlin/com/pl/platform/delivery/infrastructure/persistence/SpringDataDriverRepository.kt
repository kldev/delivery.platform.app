package com.pl.platform.delivery.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface SpringDataDriverRepository : JpaRepository<DriverJpaEntity, UUID> {
    fun existsByPhoneNumber(phoneNumber: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun findAllById(driverId: UUID): List<DriverJpaEntity>

    @Query("SELECT id FROM drivers ORDER BY random() LIMIT 1", nativeQuery = true)
    fun findRandomId(): UUID?
}

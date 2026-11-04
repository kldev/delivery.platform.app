package com.pl.platform.settlement.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpringDataSettlementRepository : JpaRepository<SettlementJpaEntity, UUID> {
    fun existsByDeliveryId(deliveryId: UUID): Boolean
}

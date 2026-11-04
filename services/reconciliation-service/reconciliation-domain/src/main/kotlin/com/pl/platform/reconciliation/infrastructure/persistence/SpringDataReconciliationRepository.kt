package com.pl.platform.reconciliation.infrastructure.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface SpringDataReconciliationRepository : JpaRepository<ReconciliationJpaEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReconciliationJpaEntity r WHERE r.deliveryId = :deliveryId")
    fun findByDeliveryIdForUpdate(deliveryId: UUID): ReconciliationJpaEntity?
}

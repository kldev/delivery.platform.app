package com.pl.platform.reconciliation.infrastructure.persistence

import com.pl.platform.reconciliation.application.port.ReconciliationRepository
import com.pl.platform.reconciliation.domain.Reconciliation
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JpaReconciliationRepository(
    private val repository: SpringDataReconciliationRepository,
) : ReconciliationRepository {
    override fun findByDeliveryIdForUpdate(deliveryId: UUID): Reconciliation? =
        repository.findByDeliveryIdForUpdate(deliveryId)?.toDomain()

    // Flushed now: a concurrent first event for the same delivery fails here on the unique delivery_id,
    // inside the handler, and the Kafka error handler retries it (then it finds the row and updates it).
    override fun create(reconciliation: Reconciliation) {
        repository.saveAndFlush(ReconciliationJpaEntity.fromDomain(reconciliation))
    }

    override fun update(reconciliation: Reconciliation) {
        val entity =
            repository.findById(reconciliation.id.value).orElseThrow {
                IllegalStateException("Reconciliation ${reconciliation.id.value} not found")
            }
        entity.updateFrom(reconciliation)
        repository.save(entity)
    }
}

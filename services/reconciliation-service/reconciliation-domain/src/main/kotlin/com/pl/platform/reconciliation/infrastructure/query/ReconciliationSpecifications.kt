package com.pl.platform.reconciliation.infrastructure.query

import com.pl.platform.reconciliation.application.listreconciliations.ListReconciliationsQuery
import com.pl.platform.reconciliation.domain.ReconciliationStatus
import com.pl.platform.reconciliation.infrastructure.persistence.ReconciliationJpaEntity
import org.springframework.data.jpa.domain.Specification
import java.util.UUID

object ReconciliationSpecifications {
    fun build(query: ListReconciliationsQuery): Specification<ReconciliationJpaEntity> =
        listOfNotNull(
            query.status?.let { status -> equal<ReconciliationStatus>("status", status) },
            query.deliveryId?.let { equal<UUID>("deliveryId", it) },
            query.settlementId?.let { equal<UUID>("settlementId", it) },
            query.paymentId?.let { equal<UUID>("paymentId", it) },
            // Exact match (uses ix_reconciliations_external_transaction_id); was LIKE '%value%' with unescaped input.
            query.externalTransactionId?.takeIf { it.isNotBlank() }?.let { equal<String>("externalTransactionId", it) },
        ).fold(Specification { _, _, _ -> null }) { result, specification -> result.and(specification) }

    private fun <T : Any> equal(
        attribute: String,
        value: T,
    ): Specification<ReconciliationJpaEntity> = Specification { root, _, cb -> cb.equal(root.get<T>(attribute), value) }
}

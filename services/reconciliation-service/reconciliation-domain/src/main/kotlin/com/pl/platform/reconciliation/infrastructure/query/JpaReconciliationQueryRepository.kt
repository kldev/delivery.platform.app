package com.pl.platform.reconciliation.infrastructure.query

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.reconciliation.application.listreconciliations.ListReconciliationsQuery
import com.pl.platform.reconciliation.application.port.ReconciliationDetails
import com.pl.platform.reconciliation.application.port.ReconciliationQueryRepository
import com.pl.platform.reconciliation.infrastructure.persistence.ReconciliationJpaEntity
import jakarta.persistence.EntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class JpaReconciliationQueryRepository(
    entityManager: EntityManager,
) : AbstractJpaSliceQueryRepository<ReconciliationJpaEntity, ListReconciliationsQuery, ReconciliationDetails>(entityManager),
    ReconciliationQueryRepository {
    override fun findById(id: UUID): ReconciliationDetails? = entityManager.find(ReconciliationJpaEntity::class.java, id)?.let(::from)

    override fun search(query: ListReconciliationsQuery): SliceResponse<ReconciliationDetails> =
        searchSlice(
            query,
            PageRequest.of(query.page.page, query.page.size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
        )

    override fun entityType(): Class<ReconciliationJpaEntity> = ReconciliationJpaEntity::class.java

    override fun specification(query: ListReconciliationsQuery): Specification<ReconciliationJpaEntity> =
        ReconciliationSpecifications.build(query)

    override fun from(entity: ReconciliationJpaEntity): ReconciliationDetails =
        ReconciliationDetails(
            id = entity.id,
            deliveryId = entity.deliveryId,
            settlementId = entity.settlementId,
            paymentId = entity.paymentId,
            externalTransactionId = entity.externalTransactionId,
            expectedAmount = entity.expectedAmount,
            actualAmount = entity.actualAmount,
            difference = entity.difference,
            currency = entity.currency,
            status = entity.status,
            createdAt = entity.createdAt,
            reconciledAt = entity.reconciledAt,
        )
}

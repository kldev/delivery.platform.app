package com.pl.platform.delivery.infrastructure.query

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.delivery.application.liststatusdurations.DeliveryStatusDuration
import com.pl.platform.delivery.application.liststatusdurations.ListStatusDurationsQuery
import com.pl.platform.delivery.application.port.DeliveryStatusDurationQueryRepository
import jakarta.persistence.EntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component

@Component
class JpaDeliveryStatusDurationQueryRepository(
    entityManager: EntityManager,
) : AbstractJpaSliceQueryRepository<DeliveryStatusDurationJpaEntity, ListStatusDurationsQuery, DeliveryStatusDuration>(
        entityManager,
    ),
    DeliveryStatusDurationQueryRepository {
    // id breaks ties: statuses changed in one transaction share status_from.
    override fun search(query: ListStatusDurationsQuery): SliceResponse<DeliveryStatusDuration> =
        searchSlice(
            query,
            PageRequest.of(query.page.page, query.page.size, Sort.by(Sort.Order.asc("statusFrom"), Sort.Order.asc("id"))),
        )

    override fun entityType(): Class<DeliveryStatusDurationJpaEntity> = DeliveryStatusDurationJpaEntity::class.java

    override fun specification(query: ListStatusDurationsQuery): Specification<DeliveryStatusDurationJpaEntity> =
        DeliveryStatusDurationSpecifications.build(query)

    override fun from(entity: DeliveryStatusDurationJpaEntity): DeliveryStatusDuration =
        DeliveryStatusDuration(
            id = entity.id,
            deliveryId = entity.deliveryId,
            status = entity.status,
            previousStatus = entity.previousStatus,
            statusFrom = entity.statusFrom,
            statusTo = entity.statusTo,
            durationSeconds = entity.durationSeconds,
        )
}

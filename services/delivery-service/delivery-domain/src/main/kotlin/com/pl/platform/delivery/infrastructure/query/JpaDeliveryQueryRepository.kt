package com.pl.platform.delivery.infrastructure.query

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.delivery.application.listdeliveries.ListDeliveriesQuery
import com.pl.platform.delivery.application.port.DeliveryDetails
import com.pl.platform.delivery.application.port.DeliveryQueryRepository
import com.pl.platform.delivery.infrastructure.persistence.DeliveryJpaEntity
import jakarta.persistence.EntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class JpaDeliveryQueryRepository(
    entityManager: EntityManager,
) : AbstractJpaSliceQueryRepository<DeliveryJpaEntity, ListDeliveriesQuery, DeliveryDetails>(entityManager),
    DeliveryQueryRepository {
    override fun findById(id: UUID): DeliveryDetails? = entityManager.find(DeliveryJpaEntity::class.java, id)?.let(::from)

    // id breaks ties so paging is deterministic for deliveries created at the same instant.
    override fun search(query: ListDeliveriesQuery): SliceResponse<DeliveryDetails> =
        searchSlice(
            query,
            PageRequest.of(query.page.page, query.page.size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
        )

    override fun entityType(): Class<DeliveryJpaEntity> = DeliveryJpaEntity::class.java

    override fun specification(query: ListDeliveriesQuery): Specification<DeliveryJpaEntity> =
        DeliverySpecifications.buildSpecifications(query)

    override fun from(entity: DeliveryJpaEntity): DeliveryDetails =
        DeliveryDetails(
            id = entity.id,
            driverId = entity.driverId,
            pickupAddress = entity.pickupAddress,
            deliveryAddress = entity.deliveryAddress,
            price = entity.price,
            status = entity.status.toDomain(),
            distanceKm = entity.distanceKm,
            currency = entity.currency ?: "PLN",
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
}

package com.pl.platform.settlement.infrastructure.query

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.settlement.application.listsettlements.ListSettlementsQuery
import com.pl.platform.settlement.application.port.SettlementDetails
import com.pl.platform.settlement.application.port.SettlementQueryRepository
import com.pl.platform.settlement.infrastructure.persistence.SettlementJpaEntity
import jakarta.persistence.EntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class JpaSettlementQueryRepository(
    entityManager: EntityManager,
) : AbstractJpaSliceQueryRepository<SettlementJpaEntity, ListSettlementsQuery, SettlementDetails>(entityManager),
    SettlementQueryRepository {
    override fun findById(id: UUID): SettlementDetails? = entityManager.find(SettlementJpaEntity::class.java, id)?.let(::from)

    override fun search(query: ListSettlementsQuery): SliceResponse<SettlementDetails> =
        searchSlice(
            query,
            PageRequest.of(query.page.page, query.page.size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
        )

    override fun entityType(): Class<SettlementJpaEntity> = SettlementJpaEntity::class.java

    override fun specification(query: ListSettlementsQuery): Specification<SettlementJpaEntity> = SettlementSpecifications.build(query)

    override fun from(entity: SettlementJpaEntity): SettlementDetails =
        SettlementDetails(
            id = entity.id,
            deliveryId = entity.deliveryId,
            driverId = entity.driverId,
            driverFullName = entity.driverFullName,
            deliveryAmount = entity.deliveryAmount,
            currency = entity.currency,
            basePercentage = entity.basePercentage,
            nightPercentage = entity.nightPercentage,
            weekendPercentage = entity.weekendPercentage,
            distancePercentage = entity.distancePercentage,
            totalPercentage = entity.totalPercentage,
            driverAmount = entity.driverAmount,
            distanceKm = entity.distanceKm,
            completedAt = entity.completedAt,
            createdAt = entity.createdAt,
        )
}

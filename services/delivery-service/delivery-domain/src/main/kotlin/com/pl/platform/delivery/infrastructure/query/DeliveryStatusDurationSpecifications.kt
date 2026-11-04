package com.pl.platform.delivery.infrastructure.query

import com.pl.platform.delivery.application.liststatusdurations.ListStatusDurationsQuery
import org.springframework.data.jpa.domain.Specification
import java.util.*

object DeliveryStatusDurationSpecifications {

    fun build(
        query: ListStatusDurationsQuery,
    ): Specification<DeliveryStatusDurationJpaEntity> {
        return listOfNotNull(
            query.deliveryId
                ?.let(::deliveryIdEquals),
        ).fold(
            Specification { _, _, _ -> null }
        ) { result, specification ->
            result.and(specification)
        }
    }

    private fun deliveryIdEquals(
        deliveryId: UUID?,
    ): Specification<DeliveryStatusDurationJpaEntity>? =
        deliveryId?.let {
            Specification { root, _, cb ->
                cb.equal(
                    root.get<UUID>("deliveryId"),
                    it,
                )
            }
        }
}
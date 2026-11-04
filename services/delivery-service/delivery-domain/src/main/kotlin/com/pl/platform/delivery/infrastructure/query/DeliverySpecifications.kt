package com.pl.platform.delivery.infrastructure.query

import com.pl.platform.delivery.application.listdeliveries.ListDeliveriesQuery
import com.pl.platform.delivery.domain.DeliveryStatus
import com.pl.platform.delivery.infrastructure.persistence.DeliveryJpaEntity
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import org.springframework.data.jpa.domain.Specification
import java.util.UUID

object DeliverySpecifications {

    fun buildSpecifications(
        query: ListDeliveriesQuery,
    ): Specification<DeliveryJpaEntity> {
        return listOfNotNull(

            query.status
                ?.let(::statusEquals),

            query.deliveryId
                ?.let(::deliveryIdEquals),
            query.driverId
                ?.let(::driverIdEquals),
        ).fold(
            Specification { _, _, _ -> null }
        ) { result, specification ->
            result.and(specification)
        }
    }


    private fun statusEquals(
        status: DeliveryStatus?,
    ): Specification<DeliveryJpaEntity>? =
        status?.let {
            Specification { root, _, cb ->
                cb.equal(
                    root.get<DeliveryStatusJpa>("status"),
                    DeliveryStatusJpa.from(it),
                )
            }
        }

    private fun deliveryIdEquals(
        deliveryId: UUID,
    ): Specification<DeliveryJpaEntity> =
            Specification { root, _, cb ->
                cb.equal(
                    root.get<UUID>("id"),
                    deliveryId,
                )
            }


    private fun driverIdEquals(
        driverId: UUID,
    ): Specification<DeliveryJpaEntity> =
            Specification { root, _, cb ->
                cb.equal(
                    root.get<UUID>("driverId"),
                    driverId,
                )
            }

}
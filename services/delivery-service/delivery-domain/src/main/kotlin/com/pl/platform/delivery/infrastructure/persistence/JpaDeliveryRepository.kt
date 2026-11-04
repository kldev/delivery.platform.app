package com.pl.platform.delivery.infrastructure.persistence

import com.pl.platform.delivery.application.port.DeliveryRepository
import com.pl.platform.delivery.domain.Delivery
import com.pl.platform.delivery.domain.DeliveryId
import org.springframework.stereotype.Repository

@Repository
class JpaDeliveryRepository(
    private val repository: SpringDataDeliveryRepository
) : DeliveryRepository {

    override fun findById(id: DeliveryId): Delivery? =
        repository.findById(id.value)
            .map(DeliveryJpaEntity::toDomain)
            .orElse(null)

    override fun create(delivery: Delivery) {
        repository.save(
            DeliveryJpaEntity.create(delivery)
        )
    }

    override fun update(delivery: Delivery) {
        val entity = repository.findById(delivery.id.value)
            .orElseThrow {
                IllegalStateException(
                    "Delivery ${delivery.id.value} not found"
                )
            }

        entity.updateFrom(delivery)

        repository.save(entity)
    }
}

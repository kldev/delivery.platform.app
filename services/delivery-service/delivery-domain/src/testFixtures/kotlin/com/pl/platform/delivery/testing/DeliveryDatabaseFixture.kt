package com.pl.platform.delivery.testing

import com.pl.platform.delivery.infrastructure.persistence.DeliveryJpaEntity
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import com.pl.platform.delivery.infrastructure.persistence.SpringDataDeliveryRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

@Component
class DeliveryDatabaseFixture(
    private val deliveryRepository: SpringDataDeliveryRepository,
) {
    fun create(
        price: BigDecimal = BigDecimal("100.00"),
        pickupAddress: String = "Opole, Krakowska 10",
        deliveryAddress: String = "Wrocław, Rynek 1"
    ): UUID {

        val delivery = DeliveryTestFactory.create(
            price = price,
            pickupAddress = pickupAddress,
            deliveryAddress = deliveryAddress,
        )

        deliveryRepository.saveAndFlush(DeliveryJpaEntity.create(delivery = delivery))

        return delivery.id.value
    }

    fun changeStatus(id: UUID, status: DeliveryStatusJpa,
                     driverId: UUID? = null) {
        val delivery = deliveryRepository.findById(id)
            .orElseThrow()

        delivery.status = status
        if (driverId != null) {
            delivery.driverId = driverId
        }

        deliveryRepository.saveAndFlush(delivery)

    }
}
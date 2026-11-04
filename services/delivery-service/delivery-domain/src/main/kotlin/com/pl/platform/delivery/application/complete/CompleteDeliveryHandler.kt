package com.pl.platform.delivery.application.complete

import com.pl.platform.common.messaging.event.delivery.DeliveryCompletedEvent
import com.pl.platform.common.messaging.port.OutboxRepository
import com.pl.platform.delivery.application.port.DeliveryRepository
import com.pl.platform.delivery.application.port.DeliveryState
import com.pl.platform.delivery.application.port.DriverRepository
import com.pl.platform.delivery.domain.DriverId
import com.pl.platform.delivery.domain.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CompleteDeliveryHandler(
    private val deliveryRepository: DeliveryRepository,
    private val driverRepository: DriverRepository,
    private val outboxRepository: OutboxRepository,
) {
    @Transactional
    fun handle(command: CompleteDeliveryCommand): DeliveryState {
        val delivery =
            deliveryRepository.findById(command.deliveryId)
                ?: throw NotFoundException("delivery", command.deliveryId.value)

        // A delivery without a driver cannot be in transit, so this is a broken invariant, not bad input.
        val driverId = checkNotNull(delivery.driverId) { "Delivery ${delivery.id.value} has no driver" }
        val driver =
            driverRepository.findById(DriverId(driverId))
                ?: throw NotFoundException("driver", driverId)

        delivery.deliver()
        deliveryRepository.update(delivery)

        outboxRepository.save(
            DeliveryCompletedEvent(
                deliveryId = delivery.id.value,
                distanceKm = delivery.distanceKm,
                price = delivery.price,
                driverId = driver.id.value,
                driverFullName = driver.fullName(),
            ),
        )

        return DeliveryState.from(delivery)
    }
}

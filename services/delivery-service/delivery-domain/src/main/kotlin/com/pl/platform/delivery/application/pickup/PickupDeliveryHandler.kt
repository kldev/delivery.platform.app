package com.pl.platform.delivery.application.pickup

import com.pl.platform.common.messaging.event.delivery.DeliveryPickedUpEvent
import com.pl.platform.common.messaging.port.OutboxRepository
import com.pl.platform.delivery.application.port.DeliveryRepository
import com.pl.platform.delivery.application.port.DeliveryState
import com.pl.platform.delivery.domain.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PickupDeliveryHandler(
    private val deliveryRepository: DeliveryRepository,
    private val outboxRepository: OutboxRepository,
) {
    @Transactional
    fun handle(command: PickupDeliveryCommand): DeliveryState {
        val delivery =
            deliveryRepository.findById(command.deliveryId)
                ?: throw NotFoundException("delivery", command.deliveryId.value)

        delivery.pickup()
        deliveryRepository.update(delivery)

        outboxRepository.save(DeliveryPickedUpEvent(deliveryId = delivery.id.value))

        return DeliveryState.from(delivery)
    }
}

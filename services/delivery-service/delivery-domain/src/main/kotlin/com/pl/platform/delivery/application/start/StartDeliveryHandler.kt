package com.pl.platform.delivery.application.start

import com.pl.platform.common.messaging.event.delivery.DeliveryStartedEvent
import com.pl.platform.common.messaging.port.OutboxRepository
import com.pl.platform.delivery.application.port.DeliveryRepository
import com.pl.platform.delivery.application.port.DeliveryState
import com.pl.platform.delivery.domain.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StartDeliveryHandler(
    private val deliveryRepository: DeliveryRepository,
    private val outboxRepository: OutboxRepository,
) {
    @Transactional
    fun handle(command: StartDeliveryCommand): DeliveryState {
        val delivery =
            deliveryRepository.findById(command.deliveryId)
                ?: throw NotFoundException("delivery", command.deliveryId.value)

        delivery.startTransit()
        deliveryRepository.update(delivery)

        outboxRepository.save(DeliveryStartedEvent(deliveryId = delivery.id.value))

        return DeliveryState.from(delivery)
    }
}

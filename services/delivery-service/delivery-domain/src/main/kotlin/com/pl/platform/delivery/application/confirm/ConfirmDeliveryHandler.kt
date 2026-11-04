package com.pl.platform.delivery.application.confirm

import com.pl.platform.common.messaging.event.delivery.DeliveryConfirmedEvent
import com.pl.platform.common.messaging.port.OutboxRepository
import com.pl.platform.delivery.application.port.DeliveryRepository
import com.pl.platform.delivery.application.port.DeliveryState
import com.pl.platform.delivery.domain.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ConfirmDeliveryHandler(
    private val deliveryRepository: DeliveryRepository,
    private val outboxRepository: OutboxRepository,
) {
    @Transactional
    fun handle(command: ConfirmDeliveryCommand): DeliveryState {
        val delivery =
            deliveryRepository.findById(command.deliveryId)
                ?: throw NotFoundException("delivery", command.deliveryId.value)

        delivery.confirm()
        deliveryRepository.update(delivery)

        outboxRepository.save(
            DeliveryConfirmedEvent(
                deliveryId = delivery.id.value,
                price = delivery.price,
            ),
        )

        return DeliveryState.from(delivery)
    }
}

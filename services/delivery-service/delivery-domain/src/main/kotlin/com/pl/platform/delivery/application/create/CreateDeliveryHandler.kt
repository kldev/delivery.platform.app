package com.pl.platform.delivery.application.create

import com.pl.platform.common.messaging.event.delivery.DeliveryCreatedEvent
import com.pl.platform.common.messaging.port.OutboxRepository
import com.pl.platform.delivery.application.port.DeliveryRepository
import com.pl.platform.delivery.domain.Delivery
import com.pl.platform.delivery.domain.DeliveryPricing
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateDeliveryHandler(
    private val deliveryRepository: DeliveryRepository,
    private val outboxRepository: OutboxRepository,
) {
    @Transactional
    fun handle(command: CreateDeliveryCommand): CreateDeliveryResult {
        val delivery =
            Delivery.create(
                pickupAddress = command.pickupAddress.trim(),
                deliveryAddress = command.deliveryAddress.trim(),
                price = DeliveryPricing.calculate(command.distanceKm),
                distanceKm = command.distanceKm,
                currency = command.currency,
            )

        deliveryRepository.create(delivery)

        outboxRepository.save(
            DeliveryCreatedEvent(
                deliveryId = delivery.id.value,
                price = delivery.price,
            ),
        )

        return CreateDeliveryResult(
            deliveryId = delivery.id,
            price = delivery.price,
            currency = delivery.currency,
            status = delivery.status,
        )
    }
}

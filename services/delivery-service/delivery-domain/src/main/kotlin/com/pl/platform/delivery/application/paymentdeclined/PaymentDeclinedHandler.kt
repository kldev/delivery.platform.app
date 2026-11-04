package com.pl.platform.delivery.application.paymentdeclined

import com.pl.platform.common.messaging.event.delivery.DeliveryCancelledEvent
import com.pl.platform.common.messaging.port.OutboxRepository
import com.pl.platform.delivery.application.port.DeliveryRepository
import com.pl.platform.delivery.application.port.ProcessedEvents
import org.slf4j.LoggerFactory
import com.pl.platform.delivery.domain.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** A declined payment cancels the delivery. */
@Service
class PaymentDeclinedHandler(
    private val deliveryRepository: DeliveryRepository,
    private val outboxRepository: OutboxRepository,
    private val processedEvents: ProcessedEvents,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun handle(command: PaymentDeclinedCommand) {
        if (processedEvents.isProcessed(command.eventId)) {
            log.info("Event {} already processed, skipping", command.eventId)
            return
        }

        val delivery =
            deliveryRepository.findById(command.deliveryId)
                ?: throw NotFoundException("delivery", command.deliveryId.value)

        delivery.declinePayment()
        deliveryRepository.update(delivery)

        outboxRepository.save(DeliveryCancelledEvent(deliveryId = delivery.id.value))
        processedEvents.markProcessed(command.eventId, command.eventType)
    }
}

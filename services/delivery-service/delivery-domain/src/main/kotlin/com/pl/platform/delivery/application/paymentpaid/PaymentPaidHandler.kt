package com.pl.platform.delivery.application.paymentpaid

import com.pl.platform.common.messaging.event.delivery.DeliveryAssignedEvent
import com.pl.platform.common.messaging.port.OutboxRepository
import com.pl.platform.delivery.application.port.DeliveryRepository
import com.pl.platform.delivery.application.port.DriverRepository
import com.pl.platform.delivery.application.port.ProcessedEvents
import org.slf4j.LoggerFactory
import com.pl.platform.delivery.domain.NotFoundException
import com.pl.platform.delivery.domain.RuleViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** A paid delivery gets a random driver. Same steps as assigndriver, kept here so the event has its own transaction. */
@Service
class PaymentPaidHandler(
    private val deliveryRepository: DeliveryRepository,
    private val driverRepository: DriverRepository,
    private val outboxRepository: OutboxRepository,
    private val processedEvents: ProcessedEvents,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun handle(command: PaymentPaidCommand) {
        if (processedEvents.isProcessed(command.eventId)) {
            log.info("Event {} already processed, skipping", command.eventId)
            return
        }

        val delivery =
            deliveryRepository.findById(command.deliveryId)
                ?: throw NotFoundException("delivery", command.deliveryId.value)
        val driverId =
            driverRepository.findRandomId()
                ?: throw RuleViolationException("driver.none-available", "There is no driver to assign")

        delivery.assign(driverId.value)
        deliveryRepository.update(delivery)

        outboxRepository.save(
            DeliveryAssignedEvent(
                deliveryId = delivery.id.value,
                driverId = driverId.value,
            ),
        )
        processedEvents.markProcessed(command.eventId, command.eventType)
    }
}

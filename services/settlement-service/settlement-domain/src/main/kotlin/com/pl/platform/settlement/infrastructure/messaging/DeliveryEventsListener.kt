package com.pl.platform.settlement.infrastructure.messaging

import com.pl.platform.common.messaging.event.delivery.DeliveryCompletedEvent
import com.pl.platform.settlement.application.deliverycompleted.DeliveryCompletedCommand
import com.pl.platform.settlement.application.deliverycompleted.DeliveryCompletedHandler
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

/** Deserialize, build the command, call the action. Failures are handled by KafkaErrorHandlingConfiguration. */
@Component
@ConditionalOnProperty(prefix = "settlement", name = ["event-bus"], havingValue = "kafka")
class DeliveryEventsListener(
    private val jsonMapper: JsonMapper,
    private val deliveryCompletedHandler: DeliveryCompletedHandler,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["delivery.completed"], groupId = "settlement-service")
    fun onDeliveryCompleted(message: ConsumerRecord<String, String>) {
        val event = jsonMapper.readValue(message.value(), DeliveryCompletedEvent::class.java)

        log.info("Received delivery completed event {} for delivery {}", event.eventId, event.deliveryId)

        deliveryCompletedHandler.handle(
            DeliveryCompletedCommand(
                eventId = event.eventId,
                eventType = event.eventType,
                deliveryId = event.deliveryId,
                driverId = event.driverId,
                driverFullName = event.driverFullName,
                deliveryAmount = event.price,
                currency = event.currency,
                distanceKm = event.distanceKm,
                completedAt = event.occurredAt,
            ),
        )
    }
}

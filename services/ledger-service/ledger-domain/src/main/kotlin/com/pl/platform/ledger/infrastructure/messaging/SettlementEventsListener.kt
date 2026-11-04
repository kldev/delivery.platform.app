package com.pl.platform.ledger.infrastructure.messaging

import com.pl.platform.common.messaging.event.settlement.DriverSettlementCompletedEvent
import com.pl.platform.ledger.application.postdriversettlement.PostDriverSettlementCommand
import com.pl.platform.ledger.application.postdriversettlement.PostDriverSettlementHandler
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

/** Entry point for settlement events: deserialize, build the command, call the action. No business logic here. */
@ConditionalOnProperty(prefix = "ledger", name = ["event-bus"], havingValue = "kafka")
@Component
class SettlementEventsListener(
    private val jsonMapper: JsonMapper,
    private val postDriverSettlementHandler: PostDriverSettlementHandler,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["driver.settlement.completed"], groupId = "ledger-service")
    fun onDriverSettlementCompleted(message: ConsumerRecord<String, String>) {
        val event = jsonMapper.readValue(message.value(), DriverSettlementCompletedEvent::class.java)

        log.info("Received driver settlement completed {} for delivery {}", event.settlementId, event.deliveryId)

        postDriverSettlementHandler.handle(
            PostDriverSettlementCommand(
                eventId = event.eventId,
                eventType = event.eventType,
                settlementId = event.settlementId,
                driverId = event.driverId,
                driverFullName = event.driverFullName,
                currency = event.currency,
                amount = event.amount,
                occurredAt = event.occurredAt,
            ),
        )
    }
}

package com.pl.platform.reconciliation.infrastructure.messaging

import com.pl.platform.common.messaging.event.settlement.SettlementCreatedEvent
import com.pl.platform.reconciliation.application.settlementcreated.SettlementCreatedCommand
import com.pl.platform.reconciliation.application.settlementcreated.SettlementCreatedHandler
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

/** Deserialize, build the command, call the action. Failures are handled by KafkaErrorHandlingConfiguration. */
@Component
@ConditionalOnProperty(prefix = "reconciliation", name = ["event-bus"], havingValue = "kafka")
class SettlementEventsListener(
    private val jsonMapper: JsonMapper,
    private val settlementCreatedHandler: SettlementCreatedHandler,
) {
    @KafkaListener(topics = ["settlement.created"], groupId = "reconciliation-service")
    fun onSettlementCreated(message: ConsumerRecord<String, String>) {
        val event = jsonMapper.readValue(message.value(), SettlementCreatedEvent::class.java)
        settlementCreatedHandler.handle(
            SettlementCreatedCommand(
                eventId = event.eventId,
                eventType = event.eventType,
                settlementId = event.settlementId,
                deliveryId = event.deliveryId,
                expectedAmount = event.deliveryAmount,
                currency = event.currency,
            ),
        )
    }
}

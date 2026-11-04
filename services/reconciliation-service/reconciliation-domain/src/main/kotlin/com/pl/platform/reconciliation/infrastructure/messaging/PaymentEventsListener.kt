package com.pl.platform.reconciliation.infrastructure.messaging

import com.pl.platform.common.messaging.event.payments.PaymentCompletedEvent
import com.pl.platform.reconciliation.application.paymentcompleted.PaymentCompletedCommand
import com.pl.platform.reconciliation.application.paymentcompleted.PaymentCompletedHandler
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

/** Deserialize, build the command, call the action. Failures are handled by KafkaErrorHandlingConfiguration. */
@Component
@ConditionalOnProperty(prefix = "reconciliation", name = ["event-bus"], havingValue = "kafka")
class PaymentEventsListener(
    private val jsonMapper: JsonMapper,
    private val paymentCompletedHandler: PaymentCompletedHandler,
) {
    @KafkaListener(topics = ["payment.completed"], groupId = "reconciliation-service")
    fun onPaymentCompleted(message: ConsumerRecord<String, String>) {
        val event = jsonMapper.readValue(message.value(), PaymentCompletedEvent::class.java)
        paymentCompletedHandler.handle(
            PaymentCompletedCommand(
                eventId = event.eventId,
                eventType = event.eventType,
                paymentId = event.paymentId,
                deliveryId = event.deliveryId,
                externalTransactionId = event.externalTransactionId,
                amount = event.amount,
                currency = event.currency,
            ),
        )
    }
}

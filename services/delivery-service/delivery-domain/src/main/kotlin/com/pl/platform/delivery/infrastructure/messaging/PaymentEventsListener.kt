package com.pl.platform.delivery.infrastructure.messaging

import com.pl.platform.common.messaging.event.payments.PaymentDeclinedEvent
import com.pl.platform.common.messaging.event.payments.PaymentPaidEvent
import com.pl.platform.delivery.application.paymentdeclined.PaymentDeclinedCommand
import com.pl.platform.delivery.application.paymentdeclined.PaymentDeclinedHandler
import com.pl.platform.delivery.application.paymentpaid.PaymentPaidCommand
import com.pl.platform.delivery.application.paymentpaid.PaymentPaidHandler
import com.pl.platform.delivery.domain.DeliveryId
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

/** Entry point for payment events: deserialize, build the command, call the action. No business logic here. */
@ConditionalOnProperty(
    prefix = "delivery",
    name = ["event-bus"],
    havingValue = "kafka",
)
@Component
class PaymentEventsListener(
    private val jsonMapper: JsonMapper,
    private val paymentPaidHandler: PaymentPaidHandler,
    private val paymentDeclinedHandler: PaymentDeclinedHandler,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["payment.paid"],
        groupId = "delivery-service",
    )
    fun onPaymentPaid(message: ConsumerRecord<String, String>) {
        val event = jsonMapper.readValue(message.value(), PaymentPaidEvent::class.java)

        log.info("Received payment paid event {} for delivery {}", event.eventId, event.deliveryId)

        paymentPaidHandler.handle(
            PaymentPaidCommand(
                eventId = event.eventId,
                eventType = event.eventType,
                deliveryId = DeliveryId(event.deliveryId),
            ),
        )
    }

    @KafkaListener(
        topics = ["payment.declined"],
        groupId = "delivery-service",
    )
    fun onPaymentDeclined(message: ConsumerRecord<String, String>) {
        val event = jsonMapper.readValue(message.value(), PaymentDeclinedEvent::class.java)

        log.info("Received payment declined event {} for delivery {}", event.eventId, event.deliveryId)

        paymentDeclinedHandler.handle(
            PaymentDeclinedCommand(
                eventId = event.eventId,
                eventType = event.eventType,
                deliveryId = DeliveryId(event.deliveryId),
            ),
        )
    }
}

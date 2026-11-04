package com.pl.platform.delivery.infrastructure.messaging.outbox

import com.pl.platform.common.messaging.OutboxMessage
import com.pl.platform.common.messaging.port.EventPublisher
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    prefix = "delivery",
    name = ["event-bus"],
    havingValue = "kafka"
)
class KafkaEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val eventToTopic: EventToTopic
) : EventPublisher {

    companion object {
        const val HEADER_EVENT_VERSION = "event-version"
    }

    override fun publish(event: OutboxMessage) {

        val record = ProducerRecord(
            eventToTopic.map(event.eventType, event.module),
            event.aggregateId.toString(),
            event.payload
        )

        record.headers().add(
            HEADER_EVENT_VERSION,
            "1".toByteArray()
        )

        kafkaTemplate.send(record).get()
    }

}
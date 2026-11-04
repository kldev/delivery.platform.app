package com.pl.platform.delivery.infrastructure.messaging.outbox

import com.pl.platform.common.messaging.OutboxMessage
import com.pl.platform.common.messaging.port.EventPublisher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.concurrent.CopyOnWriteArrayList

@Component
@ConditionalOnProperty(
    prefix = "delivery",
    name = ["event-bus"],
    havingValue = "in-memory"
)
class InMemoryEventPublisher : EventPublisher {

    private val publishedEvents =
        CopyOnWriteArrayList<OutboxMessage>()

    override fun publish(event: OutboxMessage) {
        publishedEvents += event
    }

    fun events(): List<OutboxMessage> =
        publishedEvents.toList()

    fun clear() {
        publishedEvents.clear()
    }
}
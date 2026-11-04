package com.pl.platform.delivery.infrastructure.messaging.outbox

import com.pl.platform.common.messaging.event.delivery.DeliveryEventType
import org.springframework.stereotype.Component

@Component
class EventToTopic {
    fun map(eventType: String, module: String): String {
        return when (eventType) {
            DeliveryEventType.CONFIRMED.value -> DeliveryEventType.CONFIRMED.value
            DeliveryEventType.COMPLETED.value -> DeliveryEventType.COMPLETED.value
            else -> "$module.events"
        }
    }
}
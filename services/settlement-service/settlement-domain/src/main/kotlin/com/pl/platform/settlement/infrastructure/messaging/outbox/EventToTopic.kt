package com.pl.platform.settlement.infrastructure.messaging.outbox

import org.springframework.stereotype.Component

@Component
class EventToTopic {
    fun map(eventType: String, module: String): String {
        return when (eventType) {
            "settlement.created" -> "settlement.created"
            "driver.settlement.completed" -> "driver.settlement.completed"
            else -> "$module.events"
        }
    }
}
package com.pl.platform.settlement.infrastructure.messaging.inbox

import com.pl.platform.settlement.application.port.ProcessedEvents
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class JpaProcessedEvents(
    private val repository: SpringDataProcessedEventRepository,
) : ProcessedEvents {
    override fun isProcessed(eventId: UUID): Boolean = repository.existsByEventId(eventId)

    override fun markProcessed(
        eventId: UUID,
        eventType: String,
    ) {
        repository.save(ProcessedEventJpaEntity.create(eventId = eventId, eventType = eventType))
    }
}

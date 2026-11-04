package com.pl.platform.delivery.infrastructure.messaging.inbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "processed_events")
class ProcessedEventJpaEntity(

    @Id
    @Column(name = "event_id", nullable = false)
    var eventId: UUID,

    @Column(name = "event_type", nullable = false)
    var eventType: String,

    @Column(name = "processed_at", nullable = false)
    var processedAt: Instant,
)
{
    companion object {
        fun create(
            eventId: UUID,
            eventType: String,
            processedAt: Instant = Instant.now(),
        ): ProcessedEventJpaEntity =
            ProcessedEventJpaEntity(
                eventId = eventId,
                eventType = eventType,
                processedAt = processedAt,
            )
    }
}
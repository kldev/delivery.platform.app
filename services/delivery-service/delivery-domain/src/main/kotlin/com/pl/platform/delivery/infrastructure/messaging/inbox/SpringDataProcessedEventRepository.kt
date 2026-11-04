package com.pl.platform.delivery.infrastructure.messaging.inbox

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpringDataProcessedEventRepository :
    JpaRepository<ProcessedEventJpaEntity, UUID>
{
    fun existsByEventId(eventId: UUID): Boolean
}
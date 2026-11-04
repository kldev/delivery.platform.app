package com.pl.platform.common.messaging.event.settlement

import com.pl.platform.common.messaging.event.Event
import java.time.Instant
import java.util.UUID

abstract class SettlementEvent(
    open val settlementId: UUID,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val module: String = "settlement",
    override val aggregateId: UUID = settlementId,
    override val eventType: String
) : Event
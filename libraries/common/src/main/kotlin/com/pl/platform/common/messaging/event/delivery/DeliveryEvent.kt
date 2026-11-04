package com.pl.platform.common.messaging.event.delivery

import com.pl.platform.common.messaging.event.Event
import java.time.Instant
import java.util.UUID

abstract class DeliveryEvent(
         open val deliveryId: UUID,
         override val eventId: UUID = UUID.randomUUID(),
         override val occurredAt: Instant = Instant.now(),
         override val module: String = "delivery",
         override val aggregateId: UUID = deliveryId,
         override val eventType: String
) : Event


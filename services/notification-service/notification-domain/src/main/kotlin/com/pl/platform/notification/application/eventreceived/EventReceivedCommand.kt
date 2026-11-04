package com.pl.platform.notification.application.eventreceived

import java.util.UUID

/** Any integration event the service listens to; [payload] is the event as JSON. */
data class EventReceivedCommand(
    val eventId: UUID,
    val eventType: String,
    val payload: String,
)

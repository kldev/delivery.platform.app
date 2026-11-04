package com.pl.platform.common.messaging.event.delivery

fun DeliveryEvent.eventType(): DeliveryEventType =
    when (this) {
        is DeliveryCreatedEvent -> DeliveryEventType.CREATED
        is DeliveryAssignedEvent -> DeliveryEventType.ASSIGNED
        is DeliveryPickedUpEvent -> DeliveryEventType.PICKED_UP
        is DeliveryStartedEvent -> DeliveryEventType.STARTED
        is DeliveryCompletedEvent -> DeliveryEventType.COMPLETED
        is DeliveryCancelledEvent -> DeliveryEventType.CANCELLED
        else -> throw IllegalArgumentException("Unsupported event type: $this")
    }
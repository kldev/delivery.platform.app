package com.pl.platform.notification.domain

import com.pl.platform.common.messaging.event.delivery.DeliveryEventType
import com.pl.platform.common.messaging.event.payments.PaymentEventType

/** Which channels an event type is announced on; other event types produce no notification. */
object NotificationPolicy {
    private val rules =
        mapOf(
            DeliveryEventType.COMPLETED.value to setOf(NotificationChannel.EMAIL),
            DeliveryEventType.ASSIGNED.value to setOf(NotificationChannel.EMAIL, NotificationChannel.SMS),
            PaymentEventType.Completed.value to setOf(NotificationChannel.EMAIL, NotificationChannel.PUSH),
            PaymentEventType.Declined.value to setOf(NotificationChannel.EMAIL, NotificationChannel.SMS, NotificationChannel.PUSH),
        )

    fun channelsFor(eventType: String): Set<NotificationChannel> = rules[eventType].orEmpty()
}

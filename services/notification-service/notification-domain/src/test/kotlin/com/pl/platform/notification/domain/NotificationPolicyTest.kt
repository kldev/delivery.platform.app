package com.pl.platform.notification.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NotificationPolicyTest {
    @Test
    fun `an assigned delivery is announced by email and SMS`() {
        assertThat(NotificationPolicy.channelsFor("delivery.assigned"))
            .containsExactlyInAnyOrder(NotificationChannel.EMAIL, NotificationChannel.SMS)
    }

    @Test
    fun `a declined payment goes out on every channel`() {
        assertThat(NotificationPolicy.channelsFor("payment.declined")).containsExactlyInAnyOrder(*NotificationChannel.entries.toTypedArray())
    }

    @Test
    fun `event types without a rule produce no notification`() {
        assertThat(NotificationPolicy.channelsFor("delivery.created")).isEmpty()
        assertThat(NotificationPolicy.channelsFor("unknown")).isEmpty()
    }

    @Test
    fun `a new notification is pending with no attempts`() {
        val notification = Notification.pending(java.util.UUID.randomUUID(), "delivery.assigned", NotificationChannel.SMS, "{}")

        assertThat(notification.status).isEqualTo(NotificationStatus.PENDING)
        assertThat(notification.attempts).isZero()
        assertThat(notification.recipient).isNull()
    }
}

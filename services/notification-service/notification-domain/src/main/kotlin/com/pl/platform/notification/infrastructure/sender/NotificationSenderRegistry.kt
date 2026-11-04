package com.pl.platform.notification.infrastructure.sender

import com.pl.platform.notification.application.port.NotificationSender
import com.pl.platform.notification.application.port.NotificationSenders
import com.pl.platform.notification.domain.NotificationChannel
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Any
import jakarta.enterprise.inject.Instance

@ApplicationScoped
class NotificationSenderRegistry(
    @Any senders: Instance<NotificationSender>,
) : NotificationSenders {
    private val sendersByChannel = senders.associateBy { it.channel }

    override fun forChannel(channel: NotificationChannel): NotificationSender? = sendersByChannel[channel]
}

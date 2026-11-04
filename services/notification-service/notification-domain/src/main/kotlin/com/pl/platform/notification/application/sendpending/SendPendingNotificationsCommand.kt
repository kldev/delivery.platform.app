package com.pl.platform.notification.application.sendpending

import java.time.Duration

/** [lease]: how long a claimed notification may stay PROCESSING before another run takes it over. */
data class SendPendingNotificationsCommand(
    val batchSize: Int,
    val lease: Duration,
)

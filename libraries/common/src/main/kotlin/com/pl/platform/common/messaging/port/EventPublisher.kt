package com.pl.platform.common.messaging.port

import com.pl.platform.common.messaging.OutboxMessage

interface EventPublisher {

    fun publish(event: OutboxMessage)
}
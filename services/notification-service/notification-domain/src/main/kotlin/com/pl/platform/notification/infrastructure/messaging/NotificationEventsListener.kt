package com.pl.platform.notification.infrastructure.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.pl.platform.common.messaging.event.EventMetadata
import com.pl.platform.notification.application.eventreceived.EventReceivedCommand
import com.pl.platform.notification.application.eventreceived.EventReceivedHandler
import io.quarkus.arc.properties.IfBuildProperty
import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Message
import java.time.Duration

/**
 * Entry point for every subscribed topic: read the event header, call the action, ack. No business logic here.
 *
 * A failing event must never fail the returned Uni: that stops the whole channel, whatever the failure strategy.
 * Instead a failed handler call is retried (safe: the inbox row is in the handler's transaction), and a message
 * that still fails, or cannot be read at all, is nacked. The channel's failure-strategy (dead-letter-queue) then
 * moves the record to the DLQ topic with the reason in its headers, and consumption goes on.
 */
@ApplicationScoped
@IfBuildProperty(name = "notifications.consumer.enabled", stringValue = "true")
class NotificationEventsListener(
    private val objectMapper: ObjectMapper,
    private val eventReceivedHandler: EventReceivedHandler,
    @ConfigProperty(name = "notifications.consumer.retries", defaultValue = "3") private val retries: Long,
    @ConfigProperty(name = "notifications.consumer.retry-backoff", defaultValue = "200ms") private val retryBackoff: Duration,
) {
    @Incoming("notifications")
    fun consume(message: Message<String>): Uni<Void> =
        Uni
            .createFrom()
            .item { readEvent(message.payload) }
            .flatMap { event ->
                eventReceivedHandler
                    .handle(event)
                    .onFailure()
                    .retry()
                    .withBackOff(retryBackoff, retryBackoff.multipliedBy(10))
                    .atMost(retries)
            }.ifNoItem()
            .after(PROCESSING_TIMEOUT)
            .fail()
            .flatMap { Uni.createFrom().completionStage(message.ack()) }
            .onFailure()
            .recoverWithUni { error ->
                Log.errorf(error, "Event moved to the dead letter queue: %s", message.payload)
                Uni.createFrom().completionStage(message.nack(error))
            }

    // Not retried: a payload that cannot be read will not become readable.
    private fun readEvent(payload: String): EventReceivedCommand {
        val event = objectMapper.readValue(payload, EventMetadata::class.java)
        return EventReceivedCommand(eventId = event.eventId, eventType = event.eventType, payload = payload)
    }

    private companion object {
        val PROCESSING_TIMEOUT: Duration = Duration.ofSeconds(30)
    }
}

package com.pl.platform.notification.application.eventreceived

import com.pl.platform.notification.application.port.NotificationRepository
import com.pl.platform.notification.application.port.ProcessedEvents
import com.pl.platform.notification.application.port.UnitOfWork
import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.domain.NotificationPolicy
import io.quarkus.logging.Log
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped

/**
 * Queues one notification per channel the policy assigns to the event type. The inbox row and the
 * notifications are written in one transaction, so a redelivered event creates nothing twice.
 */
@ApplicationScoped
class EventReceivedHandler(
    private val unitOfWork: UnitOfWork,
    private val processedEvents: ProcessedEvents,
    private val notifications: NotificationRepository,
) {
    fun handle(command: EventReceivedCommand): Uni<Void> =
        unitOfWork.inTransaction { tx ->
            processedEvents
                .markProcessed(tx, command.eventId, command.eventType)
                .flatMap { firstTime ->
                    if (!firstTime) {
                        Log.infof("Event %s (%s) already processed, skipping", command.eventId, command.eventType)
                        return@flatMap Uni.createFrom().voidItem()
                    }
                    val channels = NotificationPolicy.channelsFor(command.eventType)
                    if (channels.isNotEmpty()) {
                        Log.infof("Queueing %s notifications for event %s (%s)", channels, command.eventId, command.eventType)
                    }
                    Multi
                        .createFrom()
                        .iterable(channels)
                        .onItem()
                        .transformToUniAndConcatenate { channel ->
                            notifications.insert(
                                tx,
                                Notification.pending(
                                    eventId = command.eventId,
                                    eventType = command.eventType,
                                    channel = channel,
                                    payload = command.payload,
                                ),
                            )
                        }.collect()
                        .asList()
                        .replaceWithVoid()
                }
        }
}

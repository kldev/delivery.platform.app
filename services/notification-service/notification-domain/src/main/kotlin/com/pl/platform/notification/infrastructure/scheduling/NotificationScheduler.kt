package com.pl.platform.notification.infrastructure.scheduling

import com.pl.platform.notification.application.port.BackgroundTaskStats
import com.pl.platform.notification.application.sendpending.SendPendingNotificationsCommand
import com.pl.platform.notification.application.sendpending.SendPendingNotificationsHandler
import io.quarkus.arc.properties.IfBuildProperty
import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.Duration

/** Time-based entry point: runs the sendpending action and records the run in background_task_stats. */
@IfBuildProperty(name = "notifications.consumer.enabled", stringValue = "true")
@ApplicationScoped
class NotificationScheduler(
    private val sendPendingNotificationsHandler: SendPendingNotificationsHandler,
    private val stats: BackgroundTaskStats,
    @ConfigProperty(name = "notifications.send.batch-size", defaultValue = "500") private val batchSize: Int,
    @ConfigProperty(name = "notifications.send.lease", defaultValue = "5m") private val lease: Duration,
) {
    @Scheduled(every = "{scheduler.notifications.interval}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun sendPending(): Uni<Void> {
        val startedAt = System.nanoTime()
        return sendPendingNotificationsHandler
            .handle(SendPendingNotificationsCommand(batchSize = batchSize, lease = lease))
            .call { _ -> stats.recordExecution(TASK, elapsedMs(startedAt)) }
            .onFailure()
            .call { error -> stats.recordExecution(TASK, elapsedMs(startedAt), error) }
            .onFailure()
            .invoke { error -> Log.error("Failed to send pending notifications", error) }
    }

    private fun elapsedMs(startedAt: Long): Long = (System.nanoTime() - startedAt) / 1_000_000

    private companion object {
        const val TASK = "notifications"
    }
}

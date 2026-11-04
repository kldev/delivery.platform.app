package com.pl.platform.notification.application.port

import io.smallrye.mutiny.Uni

interface BackgroundTaskStats {
    fun recordExecution(
        taskName: String,
        durationMs: Long,
        error: Throwable? = null,
    ): Uni<Void>
}

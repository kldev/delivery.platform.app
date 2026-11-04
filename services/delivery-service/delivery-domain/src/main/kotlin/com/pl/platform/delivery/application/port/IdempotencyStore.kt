package com.pl.platform.delivery.application.port

import java.time.Duration
import java.time.Instant
import java.util.UUID

/** Stored responses of idempotent requests. Must be called inside the transaction of the action. */
interface IdempotencyStore {
    /**
     * Reserves the key for this transaction. Returns false when the key is already taken (after waiting
     * up to [lockTimeout] for a concurrent transaction holding it). An expired record is replaced.
     */
    fun tryReserve(
        scope: String,
        key: UUID,
        requestHash: String,
        now: Instant,
        expiresAt: Instant,
        lockTimeout: Duration,
    ): Boolean

    fun find(
        scope: String,
        key: UUID,
    ): StoredResponse?

    fun complete(
        scope: String,
        key: UUID,
        response: StoredResponse,
    )
}

data class StoredResponse(
    val requestHash: String,
    val status: Int,
    val body: String,
    val location: String?,
)

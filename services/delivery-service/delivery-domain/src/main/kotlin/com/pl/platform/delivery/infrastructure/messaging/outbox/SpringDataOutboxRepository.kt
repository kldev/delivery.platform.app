package com.pl.platform.delivery.infrastructure.messaging.outbox

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface SpringDataOutboxRepository :
    JpaRepository<OutboxMessageJpaEntity, UUID> {

    @Query(
        value = """
            UPDATE outbox_messages
            SET locked_until = :lockedUntil
            WHERE id IN (
                SELECT id
                FROM outbox_messages
                WHERE status = 'PENDING'
                  AND next_attempt_at <= :now
                  AND (
                      locked_until IS NULL
                      OR locked_until < :now
                  )
                ORDER BY created_at
                FOR UPDATE SKIP LOCKED
                LIMIT :limit
            )
            RETURNING *
        """,
        nativeQuery = true
    )
    fun claimPending(
        @Param("now") now: Instant,
        @Param("lockedUntil") lockedUntil: Instant,
        @Param("limit") limit: Int
    ): List<OutboxMessageJpaEntity>

    @Modifying
    @Query(
        """
        UPDATE OutboxMessageJpaEntity e
        SET e.status = com.pl.platform.common.messaging.OutboxStatus.PUBLISHED,
            e.publishedAt = :publishedAt,
            e.lockedUntil = null
        WHERE e.id = :id
        """
    )
    fun markPublished(
        @Param("id") id: UUID,
        @Param("publishedAt") publishedAt: Instant
    )

    @Modifying
    @Query(
        """
        UPDATE OutboxMessageJpaEntity e
        SET e.attempts = e.attempts + 1,
            e.nextAttemptAt = :nextAttemptAt,
            e.lastError = :error,
            e.lockedUntil = null
        WHERE e.id = :id
        """
    )
    fun markFailed(
        @Param("id") id: UUID,
        @Param("nextAttemptAt") nextAttemptAt: Instant,
        @Param("error") error: String
    )

    @Modifying
    @Query(
        """
        UPDATE OutboxMessageJpaEntity e
        SET e.lastError = :error,
            e.attempts = e.attempts + 1, 
            e.lockedUntil = null,
            e.status   = com.pl.platform.common.messaging.OutboxStatus.DEAD
        WHERE e.id = :id
        """
    )
    fun markDead(@Param("id") id: UUID, @Param("error") error: String)

    fun findByEventId(eventId: UUID): OutboxMessageJpaEntity?
}
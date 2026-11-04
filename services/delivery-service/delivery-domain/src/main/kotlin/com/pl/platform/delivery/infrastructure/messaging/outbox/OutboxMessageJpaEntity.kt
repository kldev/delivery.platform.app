package com.pl.platform.delivery.infrastructure.messaging.outbox

import com.pl.platform.common.messaging.OutboxStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "outbox_messages")
class OutboxMessageJpaEntity(

    @Id
    var id: UUID,

    @Column(name = "aggregate_id", nullable = false)
    var aggregateId: UUID,

    @Column(name = "module", nullable = false)
    var module: String,

    @Column(name = "eventId", nullable = false)
    var eventId: UUID,

    @Column(name = "event_type", nullable = false)
    var eventType: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    var payload: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: OutboxStatus,

    @Column(name = "attempts", nullable = false)
    var attempts: Int,

    @Column(name = "next_attempt_at", nullable = false)
    var nextAttemptAt: Instant,

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,

    @Column(name = "published_at")
    var publishedAt: Instant?,

    @Column(name = "last_error")
    var lastError: String?,

    @Column(name = "locked_until")
    var lockedUntil: Instant?
)
package com.pl.platform.delivery.infrastructure.idempotency

import com.pl.platform.delivery.application.port.IdempotencyStore
import com.pl.platform.delivery.application.port.StoredResponse
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Repository
class JdbcIdempotencyStore(
    private val jdbc: NamedParameterJdbcTemplate,
) : IdempotencyStore {
    override fun tryReserve(
        scope: String,
        key: UUID,
        requestHash: String,
        now: Instant,
        expiresAt: Instant,
        lockTimeout: Duration,
    ): Boolean {
        // Bounds the wait on the primary key when a concurrent request holds the same key (SQLSTATE 55P03).
        jdbc.jdbcTemplate.execute("SET LOCAL lock_timeout = '${lockTimeout.toMillis()}ms'")

        val params =
            mapOf(
                "scope" to scope,
                "key" to key,
                "hash" to requestHash,
                "now" to Timestamp.from(now),
                "expiresAt" to Timestamp.from(expiresAt),
            )
        jdbc.update(
            "DELETE FROM idempotency_records WHERE scope = :scope AND idempotency_key = :key AND expires_at < :now",
            params,
        )
        // The reserved row is visible to other transactions only after commit, already with the response.
        val inserted =
            jdbc.update(
                """
                INSERT INTO idempotency_records
                    (scope, idempotency_key, request_hash, response_status, response_body, created_at, expires_at)
                VALUES (:scope, :key, :hash, 0, '', :now, :expiresAt)
                ON CONFLICT (scope, idempotency_key) DO NOTHING
                """.trimIndent(),
                params,
            )
        return inserted == 1
    }

    override fun find(
        scope: String,
        key: UUID,
    ): StoredResponse? =
        jdbc
            .query(
                """
                SELECT request_hash, response_status, response_body, location
                FROM idempotency_records
                WHERE scope = :scope AND idempotency_key = :key
                """.trimIndent(),
                mapOf("scope" to scope, "key" to key),
            ) { rs, _ ->
                StoredResponse(
                    requestHash = rs.getString("request_hash"),
                    status = rs.getInt("response_status"),
                    body = rs.getString("response_body"),
                    location = rs.getString("location"),
                )
            }.firstOrNull()

    override fun complete(
        scope: String,
        key: UUID,
        response: StoredResponse,
    ) {
        jdbc.update(
            """
            UPDATE idempotency_records
            SET response_status = :status, response_body = :body, location = :location
            WHERE scope = :scope AND idempotency_key = :key
            """.trimIndent(),
            mapOf(
                "scope" to scope,
                "key" to key,
                "status" to response.status,
                "body" to response.body,
                "location" to response.location,
            ),
        )
    }
}

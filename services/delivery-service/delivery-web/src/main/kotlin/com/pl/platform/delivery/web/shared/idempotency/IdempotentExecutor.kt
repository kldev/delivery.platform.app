package com.pl.platform.delivery.web.shared.idempotency

import com.pl.platform.delivery.application.port.IdempotencyStore
import com.pl.platform.delivery.application.port.StoredResponse
import com.pl.platform.delivery.application.shared.idempotency.IdempotencyProperties
import com.pl.platform.delivery.application.shared.idempotency.RequestHasher
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.util.UUID

/**
 * Runs an action once per Idempotency-Key. The key record and the business change share one transaction:
 * they commit together or roll back together, so a failed request can be retried with the same key.
 * The action's handler must join this transaction (REQUIRED), never REQUIRES_NEW.
 */
@Component
class IdempotentExecutor(
    private val store: IdempotencyStore,
    private val tx: TransactionTemplate,
    private val json: JsonMapper,
    private val hasher: RequestHasher,
    private val clock: Clock,
    private val properties: IdempotencyProperties,
) {
    fun <T : Any> execute(
        scope: String,
        key: UUID,
        request: Any,
        responseType: Class<T>,
        action: () -> ResponseEntity<T>,
    ): ResponseEntity<T> {
        // Hash of the deserialized request: formatting or field order of the JSON does not make it "different".
        val hash = hasher.hash(json.writeValueAsBytes(request))
        return checkNotNull(
            tx.execute {
                val now = Instant.now(clock)
                val reserved =
                    store.tryReserve(scope, key, hash, now, now.plus(properties.ttl), properties.lockTimeout)
                if (!reserved) {
                    return@execute replay(checkNotNull(store.find(scope, key)), hash, responseType)
                }
                val response = action()
                store.complete(
                    scope,
                    key,
                    StoredResponse(
                        requestHash = hash,
                        status = response.statusCode.value(),
                        body = json.writeValueAsString(response.body),
                        location = response.headers.location?.toString(),
                    ),
                )
                response
            },
        )
    }

    private fun <T : Any> replay(
        stored: StoredResponse,
        hash: String,
        type: Class<T>,
    ): ResponseEntity<T> {
        if (stored.requestHash != hash) throw IdempotencyKeyReusedException()
        val builder = ResponseEntity.status(stored.status).header(REPLAYED_HEADER, "true")
        stored.location?.let { builder.location(URI.create(it)) }
        return builder.body(json.readValue(stored.body, type))
    }

    companion object {
        const val HEADER = "Idempotency-Key"
        const val REPLAYED_HEADER = "Idempotent-Replayed"
    }
}

inline fun <reified T : Any> IdempotentExecutor.execute(
    scope: String,
    key: UUID,
    request: Any,
    noinline action: () -> ResponseEntity<T>,
): ResponseEntity<T> = execute(scope, key, request, T::class.java, action)

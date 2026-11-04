package com.pl.platform.notification.infrastructure.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.pl.platform.common.messaging.event.delivery.DeliveryAssignedEvent
import com.pl.platform.notification.application.eventreceived.EventReceivedCommand
import com.pl.platform.notification.application.eventreceived.EventReceivedHandler
import com.pl.platform.notification.application.port.NotificationRepository
import com.pl.platform.notification.application.port.ProcessedEvents
import com.pl.platform.notification.application.port.UnitOfWork
import com.pl.platform.notification.testing.BaseIntegrationTest
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.microprofile.reactive.messaging.Message
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

/** The listener bean is not built in tests (no broker), so it is created here around the real handler. */
@QuarkusTest
class NotificationEventsListenerTest : BaseIntegrationTest() {
    @Inject
    lateinit var objectMapper: ObjectMapper

    @Inject
    lateinit var handler: EventReceivedHandler

    @Inject
    lateinit var unitOfWork: UnitOfWork

    @Inject
    lateinit var processedEvents: ProcessedEvents

    @Inject
    lateinit var notifications: NotificationRepository

    @Test
    fun `a handled event is acked`() {
        val message = TrackedMessage(event())

        listener(handler).consume(message.message).await().indefinitely()

        assertThat(message.acked).isTrue()
        assertThat(message.nackReason).isNull()
    }

    @Test
    fun `an unreadable payload is nacked at once, the stream does not fail`() {
        val calls = AtomicInteger()
        val message = TrackedMessage("not json")

        listener(failing(calls, failures = 0)).consume(message.message).await().indefinitely()

        assertThat(message.acked).isFalse()
        assertThat(message.nackReason).isNotNull()
        assertThat(calls.get()).isZero()
    }

    @Test
    fun `a handler failure is retried and acked when a retry succeeds`() {
        val calls = AtomicInteger()
        val message = TrackedMessage(event())

        listener(failing(calls, failures = 2)).consume(message.message).await().indefinitely()

        assertThat(message.acked).isTrue()
        assertThat(calls.get()).isEqualTo(3)
    }

    @Test
    fun `a handler that keeps failing is nacked after the retries, the stream does not fail`() {
        val calls = AtomicInteger()
        val message = TrackedMessage(event())

        listener(failing(calls, failures = Int.MAX_VALUE)).consume(message.message).await().indefinitely()

        assertThat(message.acked).isFalse()
        assertThat(message.nackReason).hasMessage("database down")
        assertThat(calls.get()).isEqualTo(1 + RETRIES.toInt())
    }

    private fun listener(handler: EventReceivedHandler) =
        NotificationEventsListener(objectMapper, handler, RETRIES, Duration.ofMillis(1))

    /** A handler that fails [failures] times, then delegates to the real one. */
    private fun failing(
        calls: AtomicInteger,
        failures: Int,
    ) = object : EventReceivedHandler(unitOfWork, processedEvents, notifications) {
        override fun handle(command: EventReceivedCommand): Uni<Void> =
            Uni.createFrom().deferred {
                if (calls.incrementAndGet() <= failures) {
                    Uni.createFrom().failure(IllegalStateException("database down"))
                } else {
                    handler.handle(command)
                }
            }
    }

    // A real integration event, serialized the way the outbox of delivery-service writes it.
    private fun event() = objectMapper.writeValueAsString(DeliveryAssignedEvent(deliveryId = UUID.randomUUID(), driverId = UUID.randomUUID()))

    private class TrackedMessage(
        payload: String,
    ) {
        @Volatile var acked = false

        @Volatile var nackReason: Throwable? = null

        val message: Message<String> =
            Message.of(
                payload,
                {
                    acked = true
                    CompletableFuture.completedFuture(null)
                },
                { reason: Throwable ->
                    nackReason = reason
                    CompletableFuture.completedFuture(null)
                },
            )
    }

    private companion object {
        const val RETRIES = 3L
    }
}

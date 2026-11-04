package com.pl.platform.notification.application.eventreceived

import com.pl.platform.notification.testing.BaseIntegrationTest
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class EventReceivedHandlerTest : BaseIntegrationTest() {
    @Inject
    lateinit var handler: EventReceivedHandler

    @Test
    fun `queues one pending notification per channel of the policy`() {
        val eventId = UUID.randomUUID()

        handler.handle(event(eventId, "delivery.assigned")).await().indefinitely()

        assertThat(channelsOf(eventId)).containsExactlyInAnyOrder("EMAIL", "SMS")
        assertThat(processed(eventId)).isTrue()
    }

    @Test
    fun `the same event twice queues its notifications once`() {
        val eventId = UUID.randomUUID()

        handler.handle(event(eventId, "payment.declined")).await().indefinitely()
        handler.handle(event(eventId, "payment.declined")).await().indefinitely()

        assertThat(channelsOf(eventId)).containsExactlyInAnyOrder("EMAIL", "SMS", "PUSH")
    }

    @Test
    fun `an event type without channels is recorded and queues nothing`() {
        val eventId = UUID.randomUUID()

        handler.handle(event(eventId, "delivery.created")).await().indefinitely()

        assertThat(channelsOf(eventId)).isEmpty()
        assertThat(processed(eventId)).isTrue()
    }

    private fun event(
        eventId: UUID,
        type: String,
    ) = EventReceivedCommand(eventId, type, """{"eventId":"$eventId","eventType":"$type","deliveryId":"${UUID.randomUUID()}"}""")

    private fun channelsOf(eventId: UUID): List<String> =
        pool
            .query("SELECT channel FROM notifications WHERE event_id = '$eventId'")
            .execute()
            .await()
            .indefinitely()
            .map { it.getString("channel") }

    private fun processed(eventId: UUID): Boolean =
        pool
            .query("SELECT count(*) FROM processed_events WHERE event_id = '$eventId'")
            .execute()
            .await()
            .indefinitely()
            .first()
            .getLong(0) == 1L
}

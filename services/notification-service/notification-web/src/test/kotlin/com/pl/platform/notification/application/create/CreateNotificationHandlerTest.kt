package com.pl.platform.notification.application.create

import com.pl.platform.notification.domain.ConflictException
import com.pl.platform.notification.domain.NotificationChannel
import com.pl.platform.notification.domain.NotificationRecipient
import com.pl.platform.notification.domain.NotificationStatus
import com.pl.platform.notification.testing.BaseIntegrationTest
import com.pl.platform.notification.testing.NotificationFixture
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class CreateNotificationHandlerTest : BaseIntegrationTest() {
    @Inject
    lateinit var handler: CreateNotificationHandler

    @Inject
    lateinit var fixture: NotificationFixture

    @Test
    fun `queues a pending notification with a structured recipient`() {
        val created = handler.handle(command(UUID.randomUUID())).await().indefinitely()

        val stored = fixture.find(created.id)
        assertThat(stored.status).isEqualTo(NotificationStatus.PENDING)
        assertThat(stored.attempts).isZero()
        assertThat(stored.recipient).isEqualTo(NotificationRecipient(email = "user@example.com"))
        assertThat(stored.payload).contains("deliveryId")
    }

    @Test
    fun `a second notification for the same event and channel is a conflict`() {
        val eventId = UUID.randomUUID()
        handler.handle(command(eventId)).await().indefinitely()

        assertThatThrownBy { handler.handle(command(eventId)).await().indefinitely() }
            .isInstanceOf(ConflictException::class.java)
            .hasFieldOrPropertyWithValue("code", "notification.duplicate")
    }

    private fun command(eventId: UUID) =
        CreateNotificationCommand(
            eventId = eventId,
            eventType = "delivery.assigned",
            channel = NotificationChannel.EMAIL,
            payload = """{"deliveryId":"${UUID.randomUUID()}"}""",
            recipient = NotificationRecipient(email = "user@example.com"),
        )
}

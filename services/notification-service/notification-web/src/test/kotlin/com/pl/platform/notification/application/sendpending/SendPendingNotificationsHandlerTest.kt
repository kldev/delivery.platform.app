package com.pl.platform.notification.application.sendpending

import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.domain.NotificationChannel
import com.pl.platform.notification.domain.NotificationRecipient
import com.pl.platform.notification.domain.NotificationStatus
import com.pl.platform.notification.testing.BaseIntegrationTest
import com.pl.platform.notification.testing.FakeDriverDirectory
import com.pl.platform.notification.testing.NotificationFixture
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID

@QuarkusTest
class SendPendingNotificationsHandlerTest : BaseIntegrationTest() {
    @Inject
    lateinit var handler: SendPendingNotificationsHandler

    @Inject
    lateinit var fixture: NotificationFixture

    @Inject
    lateinit var drivers: FakeDriverDirectory

    @BeforeEach
    fun clean() {
        cleanDatabase()
    }

    @Test
    fun `a notification with a recipient is sent`() {
        val notification = fixture.create()

        run()

        val stored = fixture.find(notification.id)
        assertThat(stored.status).isEqualTo(NotificationStatus.SENT)
        assertThat(stored.attempts).isEqualTo(1)
        assertThat(stored.sentAt).isNotNull()
    }

    @Test
    fun `the driver of the delivery becomes the recipient and is saved`() {
        val deliveryId = UUID.randomUUID()
        val driver = NotificationRecipient("Ada Lovelace", "+48123123123", "ada@example.com")
        drivers.register(deliveryId, driver)
        val notification = fixture.create(recipient = null, channel = NotificationChannel.SMS, payload = """{"deliveryId":"$deliveryId"}""")

        run()

        val stored = fixture.find(notification.id)
        assertThat(stored.status).isEqualTo(NotificationStatus.SENT)
        assertThat(stored.recipient).isEqualTo(driver)
    }

    @Test
    fun `a failed attempt goes back to pending and is counted once`() {
        val notification = fixture.create(recipient = NotificationRecipient(fullName = "No Email")) // EMAIL needs an email

        run()

        val stored = fixture.find(notification.id)
        assertThat(stored.status).isEqualTo(NotificationStatus.PENDING)
        assertThat(stored.attempts).isEqualTo(1)
        assertThat(stored.lastError).contains("no email")
    }

    @Test
    fun `the last allowed attempt that fails marks the notification FAILED`() {
        val notification = fixture.create(recipient = null, payload = "{}", attempts = Notification.MAX_ATTEMPTS - 1)

        run()

        val stored = fixture.find(notification.id)
        assertThat(stored.status).isEqualTo(NotificationStatus.FAILED)
        assertThat(stored.attempts).isEqualTo(Notification.MAX_ATTEMPTS)
        assertThat(stored.lastError).isEqualTo("Recipient could not be resolved")
    }

    @Test
    fun `a notification stuck in PROCESSING past the lease is taken over, a fresh one is left alone`() {
        val stuck = fixture.create(status = NotificationStatus.PROCESSING, attempts = 1)
        val running = fixture.create(status = NotificationStatus.PROCESSING, attempts = 1)
        sql("UPDATE notifications SET claimed_at = now() - interval '10 minutes' WHERE id = '${stuck.id.value}'")
        sql("UPDATE notifications SET claimed_at = now() WHERE id = '${running.id.value}'")

        run()

        assertThat(fixture.find(stuck.id).status).isEqualTo(NotificationStatus.SENT)
        assertThat(fixture.find(stuck.id).attempts).isEqualTo(2)
        assertThat(fixture.find(running.id).status).isEqualTo(NotificationStatus.PROCESSING)
    }

    @Test
    fun `only pending notifications are claimed, oldest first, up to the batch size`() {
        val older = fixture.create(createdAt = Instant.now().minusSeconds(60))
        val newer = fixture.create()
        val sent = fixture.create(status = NotificationStatus.SENT)

        handler.handle(SendPendingNotificationsCommand(batchSize = 1, lease = Duration.ofMinutes(5))).await().indefinitely()

        assertThat(fixture.find(older.id).status).isEqualTo(NotificationStatus.SENT)
        assertThat(fixture.find(newer.id).status).isEqualTo(NotificationStatus.PENDING)
        assertThat(fixture.find(sent.id).attempts).isZero()
    }

    private fun run() {
        handler.handle(SendPendingNotificationsCommand(batchSize = 100, lease = Duration.ofMinutes(5))).await().indefinitely()
    }
}

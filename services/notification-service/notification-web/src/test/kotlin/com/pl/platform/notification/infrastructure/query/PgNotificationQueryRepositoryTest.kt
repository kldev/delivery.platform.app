package com.pl.platform.notification.infrastructure.query

import com.pl.platform.notification.application.listnotifications.ListNotificationsQuery
import com.pl.platform.notification.application.port.NotificationQueryRepository
import com.pl.platform.notification.domain.NotificationStatus
import com.pl.platform.notification.testing.BaseIntegrationTest
import com.pl.platform.notification.testing.NotificationFixture
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@QuarkusTest
class PgNotificationQueryRepositoryTest : BaseIntegrationTest() {

    @Inject
    lateinit var fixture: NotificationFixture

    @Inject
    lateinit var repository: NotificationQueryRepository

    @BeforeEach
    fun setup() {
        cleanDatabase()
    }

    @Test
    fun `should return all notifications without filters`() {

        fixture.create()
        fixture.create()
        fixture.create()

        val result = repository
            .find(ListNotificationsQuery())
            .await()
            .indefinitely().content

        assertTrue(result.size >= 3)
    }

    @Test
    fun `should filter by status`() {

        fixture.create(
            status = NotificationStatus.PENDING
        )

        fixture.create(
            status = NotificationStatus.SENT
        )

        val result = repository
            .find(
                ListNotificationsQuery(
                    status = NotificationStatus.SENT
                )
            )
            .await()
            .indefinitely().content

        assertEquals(1, result.size)
        assertEquals(
            NotificationStatus.SENT,
            result.single().status
        )
    }

    @Test
    fun `should filter by event id`() {

        val eventId = UUID.randomUUID()

        fixture.create(
            eventId = eventId
        )

        fixture.create(
            eventId = UUID.randomUUID()
        )

        val result = repository
            .find(
                ListNotificationsQuery(
                    eventId = eventId
                )
            )
            .await()
            .indefinitely().content

        assertEquals(1, result.size)
        assertEquals(
            eventId,
            result.single().eventId
        )
    }

    @Test
    fun `should filter from created at`() {

        val timezone = ZoneId.of("Europe/Warsaw")

        val from = LocalDate.now(timezone)

        fixture.create(
            createdAt = from
                .minusDays(1)
                .atStartOfDay(timezone)
                .plusHours(12)
                .toInstant()
        )

        fixture.create(
            createdAt = from
                .atStartOfDay(timezone)
                .plusHours(12)
                .toInstant()
        )

        val result = repository
            .find(
                ListNotificationsQuery(
                    from = from,
                    timezone = timezone
                )
            )
            .await()
            .indefinitely().content

        assertEquals(1, result.size)
    }

    @Test
    fun `should filter to created at`() {

        val timezone = ZoneId.of("Europe/Warsaw")
        val to = LocalDate.of(2026, 8, 25)

        fixture.create(
            createdAt = to
                .atStartOfDay(timezone)
                .plusHours(12)
                .toInstant()
        )

        fixture.create(
            createdAt = to
                .plusDays(1)
                .atStartOfDay(timezone)
                .toInstant()
        )

        val result = repository
            .find(
                ListNotificationsQuery(
                    to = to,
                    timezone = timezone
                )
            )
            .await()
            .indefinitely().content

        assertEquals(1, result.size)
    }

    @Test
    fun `should filter by from and to`() {

        val timezone = ZoneId.of("Europe/Warsaw")

        val from = LocalDate.of(2026, 8, 25)
        val to = LocalDate.of(2026, 8, 26)

        fixture.create(
            createdAt = from
                .minusDays(1)
                .atTime(12, 0)
                .atZone(timezone)
                .toInstant()
        )

        fixture.create(
            createdAt = from
                .atTime(12, 0)
                .atZone(timezone)
                .toInstant()
        )

        fixture.create(
            createdAt = to
                .atTime(12, 0)
                .atZone(timezone)
                .toInstant()
        )

        fixture.create(
            createdAt = to
                .plusDays(1)
                .atTime(12, 0)
                .atZone(timezone)
                .toInstant()
        )

        val result = repository
            .find(
                ListNotificationsQuery(
                    from = from,
                    to = to,
                    timezone = timezone
                )
            )
            .await()
            .indefinitely().content

        assertEquals(2, result.size)
    }

    @Test
    fun `should combine status and event id filters`() {

        val eventId = UUID.randomUUID()

        fixture.create(
            eventId = eventId,
            status = NotificationStatus.PENDING
        )

        fixture.create(
            eventId =  UUID.randomUUID(),
            status = NotificationStatus.SENT
        )

        fixture.create(
            eventId = UUID.randomUUID(),
            status = NotificationStatus.PENDING
        )

        val result = repository
            .find(
                ListNotificationsQuery(
                    status = NotificationStatus.PENDING,
                    eventId = eventId
                )
            )
            .await()
            .indefinitely().content

        assertEquals(1, result.size)

        assertEquals(
            eventId,
            result.single().eventId
        )

        assertEquals(
            NotificationStatus.PENDING,
            result.single().status
        )
    }

    @Test
    fun `should combine all filters`() {

        val timezone = ZoneId.of("Europe/Warsaw")

        val eventId = UUID.randomUUID()

        val from = LocalDate.of(2026, 8, 25)
        val to = LocalDate.of(2026, 8, 26)

        // MATCH
        fixture.create(
            eventId = eventId,
            status = NotificationStatus.SENT,
            createdAt = from
                .atTime(10, 0)
                .atZone(timezone)
                .toInstant()
        )

        // WRONG STATUS
        fixture.create(
            eventId =  UUID.randomUUID(),
            status = NotificationStatus.PENDING,
            createdAt = from
                .atTime(11, 0)
                .atZone(timezone)
                .toInstant()
        )

        // WRONG EVENT ID
        fixture.create(
            eventId = UUID.randomUUID(),
            status = NotificationStatus.SENT,
            createdAt = from
                .atTime(12, 0)
                .atZone(timezone)
                .toInstant()
        )

        // OUTSIDE DATE RANGE
        fixture.create(
            eventId =  UUID.randomUUID(),
            status = NotificationStatus.SENT,
            createdAt = to
                .plusDays(1)
                .atTime(10, 0)
                .atZone(timezone)
                .toInstant()
        )

        val result = repository
            .find(
                ListNotificationsQuery(
                    status = NotificationStatus.SENT,
                    eventId = eventId,
                    from = from,
                    to = to,
                    timezone = timezone
                )
            )
            .await()
            .indefinitely().content

        assertEquals(1, result.size)

        val notification = result.single()

        assertEquals(eventId, notification.eventId)
        assertEquals(NotificationStatus.SENT, notification.status)
    }

    @Test
    fun `should return empty result when filters do not match`() {

        fixture.create(
            status = NotificationStatus.PENDING
        )

        val result = repository
            .find(
                ListNotificationsQuery(
                    status = NotificationStatus.FAILED
                )
            )
            .await()
            .indefinitely().content

        assertTrue(result.isEmpty())
    }
}
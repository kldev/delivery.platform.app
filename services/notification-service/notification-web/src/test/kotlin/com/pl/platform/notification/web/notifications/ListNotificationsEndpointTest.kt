package com.pl.platform.notification.web.notifications

import com.pl.platform.notification.domain.NotificationStatus
import com.pl.platform.notification.testing.BaseIntegrationTest
import com.pl.platform.notification.testing.NotificationFixture
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

@QuarkusTest
class ListNotificationsEndpointTest : BaseIntegrationTest() {
    @Inject
    lateinit var fixture: NotificationFixture

    @BeforeEach
    fun setUp() {
        cleanDatabase()
    }

    @Test
    fun `lists notifications as a slice`() {
        fixture.create()
        fixture.create()

        given()
            .`when`()
            .get("/api/notifications")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(2))
            .body("hasNext", equalTo(false))
    }

    @Test
    fun `filters by status`() {
        fixture.create(status = NotificationStatus.PENDING)
        fixture.create(status = NotificationStatus.SENT)

        given()
            .queryParam("status", "SENT")
            .`when`()
            .get("/api/notifications")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(1))
            .body("content[0].status", equalTo("SENT"))
    }

    @Test
    fun `filters by day and pages`() {
        val timezone = ZoneId.of("Europe/Warsaw")
        val day = LocalDate.of(2026, 8, 25)
        fixture.create(createdAt = day.atTime(10, 0).atZone(timezone).toInstant())
        fixture.create(createdAt = day.atTime(11, 0).atZone(timezone).toInstant())
        fixture.create(createdAt = day.plusDays(1).atTime(11, 0).atZone(timezone).toInstant())

        given()
            .queryParam("from", "2026-08-25")
            .queryParam("to", "2026-08-25")
            .queryParam("size", 1)
            .queryParam("page", 0)
            .`when`()
            .get("/api/notifications")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(1))
            .body("hasNext", equalTo(true))
    }

    @Test
    fun `invalid parameters are 400 problems with the field`() {
        expectInvalid("status", "LOST")
        expectInvalid("timezone", "Mars/Base")
        expectInvalid("size", "0")
        expectInvalid("size", "600")
        expectInvalid("page", "-1")
        given()
            .queryParam("from", "2026-08-26")
            .queryParam("to", "2026-08-25")
            .`when`()
            .get("/api/notifications")
            .then()
            .statusCode(400)
            .body("errors[0].field", equalTo("from"))
    }

    private fun expectInvalid(
        name: String,
        value: String,
    ) {
        given()
            .queryParam(name, value)
            .`when`()
            .get("/api/notifications")
            .then()
            .statusCode(400)
            .contentType("application/problem+json")
            .body("code", equalTo("validation"))
            .body("errors[0].field", equalTo(name))
    }
}

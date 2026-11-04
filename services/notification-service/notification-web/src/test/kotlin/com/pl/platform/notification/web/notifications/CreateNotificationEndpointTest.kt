package com.pl.platform.notification.web.notifications

import com.pl.platform.notification.testing.BaseIntegrationTest
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class CreateNotificationEndpointTest : BaseIntegrationTest() {
    @Test
    fun `queues a notification and points Location at it`() {
        val eventId = UUID.randomUUID()

        val created =
            post(body(eventId))
                .then()
                .statusCode(201)
                .extract()

        val id = created.path<String>("id")
        assertThat(created.header("Location")).endsWith("/api/notifications/$id")

        given()
            .`when`()
            .get("/api/notifications/$id")
            .then()
            .statusCode(200)
            .body("eventId", equalTo(eventId.toString()))
            .body("status", equalTo("PENDING"))
            .body("channel", equalTo("EMAIL"))
            .body("recipient.email", equalTo("user@example.com"))
            .body("payload.deliveryId", equalTo("8bd6afc7-c6ac-4c01-823b-64752244c5f7"))
    }

    @Test
    fun `the same event and channel twice is a 409 problem`() {
        val eventId = UUID.randomUUID()
        post(body(eventId)).then().statusCode(201)

        post(body(eventId))
            .then()
            .statusCode(409)
            .contentType("application/problem+json")
            .body("code", equalTo("notification.duplicate"))
    }

    @Test
    fun `invalid fields are listed by name`() {
        post("""{"eventId":null,"eventType":"","channel":null,"payload":null,"recipient":{"email":"not-an-email"}}""")
            .then()
            .statusCode(400)
            .contentType("application/problem+json")
            .body("code", equalTo("validation"))
            .body("errors.field", containsInAnyOrder("eventId", "eventType", "channel", "payload", "recipient.email"))
    }

    @Test
    fun `malformed JSON is a 400 problem`() {
        post("""{ "eventId": """)
            .then()
            .statusCode(400)
            .contentType("application/problem+json")
    }

    @Test
    fun `an unknown notification is a 404 problem`() {
        given()
            .`when`()
            .get("/api/notifications/${UUID.randomUUID()}")
            .then()
            .statusCode(404)
            .contentType("application/problem+json")
            .body("code", equalTo("notification.not-found"))
    }

    private fun post(json: String) =
        given()
            .contentType(ContentType.JSON)
            .body(json)
            .`when`()
            .post("/api/notifications")

    private fun body(eventId: UUID) =
        """
        {
          "eventId": "$eventId",
          "eventType": "delivery.assigned",
          "channel": "EMAIL",
          "recipient": { "email": "user@example.com" },
          "payload": { "deliveryId": "8bd6afc7-c6ac-4c01-823b-64752244c5f7" }
        }
        """.trimIndent()
}

package com.pl.platform.notification.web

import com.pl.platform.notification.testing.BaseIntegrationTest
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

@QuarkusTest
class RootResourceTest : BaseIntegrationTest() {

    @Test
    fun testHelloEndpoint() {
        given()
          .`when`().get("/")
          .then()
             .statusCode(200)
             .body(`is`("notification-svc"))
    }

}
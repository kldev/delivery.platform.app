package com.pl.platform.delivery.web.drivers

import com.pl.platform.delivery.BaseRestIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.util.UUID

class CreateDriverEndpointTest : BaseRestIntegrationTest() {
    @Test
    fun `creates a driver and points Location at it`() {
        val phone = uniquePhone()

        val result =
            post("""{"firstName":"John","lastName":"Connor","phoneNumber":"$phone","email":"j${phone.takeLast(6)}@fake.io"}""")
                .expectStatus()
                .isCreated
                .expectBody()
                .jsonPath("$.phoneNumber")
                .isEqualTo(phone)
                .returnResult()

        val id = jsonMapper.readTree(result.responseBodyContent).get("id").asString()
        assertThat(result.responseHeaders.location.toString()).isEqualTo("/api/drivers/$id")
    }

    @Test
    fun `invalid fields are listed by name`() {
        post("""{"firstName":"","lastName":"Connor","phoneNumber":"abc","email":"not-an-email"}""")
            .expectStatus()
            .isBadRequest
            .expectHeader()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.errors[*].field")
            .value<List<String>> { assertThat(it).containsExactlyInAnyOrder("firstName", "phoneNumber", "email") }
    }

    @Test
    fun `a phone number that is taken is a 409`() {
        val phone = uniquePhone()
        post("""{"firstName":"A","lastName":"B","phoneNumber":"$phone","email":"a${phone.takeLast(6)}@fake.io"}""")
            .expectStatus()
            .isCreated

        post("""{"firstName":"C","lastName":"D","phoneNumber":"$phone","email":"c${phone.takeLast(6)}@fake.io"}""")
            .expectStatus()
            .isEqualTo(409)
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("driver.phone-number-taken")
    }

    private fun post(json: String) =
        restTestClient
            .post()
            .uri("/api/drivers")
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .exchange()

    private fun uniquePhone() = "+48" + UUID.randomUUID().mostSignificantBits.toULong().toString().take(9)
}

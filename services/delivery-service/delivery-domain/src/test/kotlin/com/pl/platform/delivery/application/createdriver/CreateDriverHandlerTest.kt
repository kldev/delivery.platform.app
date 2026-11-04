package com.pl.platform.delivery.application.createdriver

import com.pl.platform.delivery.domain.ConflictException
import com.pl.platform.delivery.testing.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class CreateDriverHandlerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var handler: CreateDriverHandler

    @Test
    fun `creates a driver with trimmed names and a lowercase email`() {
        val phone = uniquePhone()

        val driver = handler.handle(CreateDriverCommand(" Ada ", "Lovelace", phone, "Ada.${phone.takeLast(6)}@Fake.io"))

        assertThat(driver.firstName).isEqualTo("Ada")
        assertThat(driver.email).isEqualTo("ada.${phone.takeLast(6)}@fake.io")
    }

    @Test
    fun `a second driver with the same phone number is a conflict`() {
        val phone = uniquePhone()
        handler.handle(CreateDriverCommand("Ada", "Lovelace", phone, "a${phone.takeLast(6)}@fake.io"))

        assertThatThrownBy { handler.handle(CreateDriverCommand("Bob", "Smith", phone, "b${phone.takeLast(6)}@fake.io")) }
            .isInstanceOf(ConflictException::class.java)
            .hasFieldOrPropertyWithValue("code", "driver.phone-number-taken")
    }

    private fun uniquePhone() = "+48" + UUID.randomUUID().mostSignificantBits.toULong().toString().take(9)
}

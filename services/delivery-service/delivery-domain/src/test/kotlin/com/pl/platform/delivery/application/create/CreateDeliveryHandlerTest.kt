package com.pl.platform.delivery.application.create

import com.pl.platform.delivery.testing.BaseIntegrationTest
import com.pl.platform.delivery.testing.DeliveryStateFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

class CreateDeliveryHandlerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var handler: CreateDeliveryHandler

    @Autowired
    lateinit var state: DeliveryStateFixture

    @Test
    fun `creates a priced delivery and writes delivery created to the outbox`() {
        val result =
            handler.handle(
                CreateDeliveryCommand(
                    pickupAddress = " Test 1 ",
                    deliveryAddress = "Test 2",
                    distanceKm = BigDecimal("120.55"),
                ),
            )

        val id = result.deliveryId.value
        assertThat(result.price).isEqualByComparingTo("50.00")
        assertThat(state.statusOf(id)).isEqualTo("CREATED")
        assertThat(state.outboxEventTypes(id)).containsExactly("delivery.created")
    }

    @Test
    fun `rejects a non-positive distance and stores nothing`() {
        val before = jdbcTemplate.queryForObject("SELECT count(*) FROM deliveries", Long::class.java)

        runCatching {
            handler.handle(CreateDeliveryCommand(pickupAddress = "A", deliveryAddress = "B", distanceKm = BigDecimal.ZERO))
        }.also { assertThat(it.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java) }

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM deliveries", Long::class.java)).isEqualTo(before)
    }
}

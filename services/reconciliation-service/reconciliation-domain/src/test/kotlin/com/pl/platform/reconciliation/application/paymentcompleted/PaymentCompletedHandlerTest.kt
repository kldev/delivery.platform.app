package com.pl.platform.reconciliation.application.paymentcompleted

import com.pl.platform.reconciliation.testing.BaseIntegrationTest
import com.pl.platform.reconciliation.testing.ReconciliationEvents
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.util.UUID

class PaymentCompletedHandlerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var events: ReconciliationEvents

    @Test
    fun `a payment that comes first leaves the expected amount unknown`() {
        val deliveryId = UUID.randomUUID()

        events.payment(deliveryId, amount = "9.00")

        val state = events.stateOf(deliveryId)
        assertThat(state["status"]).isEqualTo("PENDING")
        assertThat(state["actual_amount"] as BigDecimal).isEqualByComparingTo("9.00")
        // Before: expected was set to the paid amount, which looked like a match before any settlement existed.
        assertThat(state["expected_amount"]).isNull()
        assertThat(state["difference"]).isNull()
    }

    @Test
    fun `a payment that differs from the settlement is a discrepancy with the difference`() {
        val deliveryId = UUID.randomUUID()
        events.settlement(deliveryId, expected = "7.00")

        events.payment(deliveryId, amount = "9.00")

        val state = events.stateOf(deliveryId)
        assertThat(state["status"]).isEqualTo("DISCREPANCY")
        assertThat(state["difference"] as BigDecimal).isEqualByComparingTo("2.00")
    }

    @Test
    fun `the same event twice is applied once`() {
        val deliveryId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val paymentId = UUID.randomUUID()

        events.payment(deliveryId, amount = "9.00", eventId = eventId, paymentId = paymentId)
        events.payment(deliveryId, amount = "9.00", eventId = eventId, paymentId = paymentId)

        assertThat(events.count(deliveryId)).isEqualTo(1)
    }
}

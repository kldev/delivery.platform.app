package com.pl.platform.reconciliation.application.settlementcreated

import com.pl.platform.reconciliation.testing.BaseIntegrationTest
import com.pl.platform.reconciliation.domain.ConflictException
import com.pl.platform.reconciliation.domain.RuleViolationException
import com.pl.platform.reconciliation.testing.ReconciliationEvents
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.util.UUID

class SettlementCreatedHandlerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var events: ReconciliationEvents

    @Test
    fun `a first settlement opens a pending reconciliation with the expected amount`() {
        val deliveryId = UUID.randomUUID()

        events.settlement(deliveryId, expected = "70.00")

        val state = events.stateOf(deliveryId)
        assertThat(state["status"]).isEqualTo("PENDING")
        assertThat(state["expected_amount"] as BigDecimal).isEqualByComparingTo("70.00")
        assertThat(state["actual_amount"]).isNull()
    }

    @Test
    fun `a settlement after a matching payment reconciles the delivery`() {
        val deliveryId = UUID.randomUUID()
        events.payment(deliveryId, amount = "70.00")

        events.settlement(deliveryId, expected = "70.00")

        val state = events.stateOf(deliveryId)
        assertThat(state["status"]).isEqualTo("RECONCILED")
        assertThat(state["difference"] as BigDecimal).isEqualByComparingTo("0")
        assertThat(state["reconciled_at"]).isNotNull()
    }

    @Test
    fun `the same event twice is applied once`() {
        val deliveryId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val settlementId = UUID.randomUUID()

        events.settlement(deliveryId, expected = "70.00", eventId = eventId, settlementId = settlementId)
        events.settlement(deliveryId, expected = "70.00", eventId = eventId, settlementId = settlementId)

        assertThat(events.count(deliveryId)).isEqualTo(1)
    }

    @Test
    fun `a settlement in another currency is rejected and changes nothing`() {
        val deliveryId = UUID.randomUUID()
        events.payment(deliveryId, amount = "70.00", currency = "PLN")

        assertThatThrownBy { events.settlement(deliveryId, expected = "70.00", currency = "EUR") }
            .isInstanceOf(RuleViolationException::class.java)
            .hasFieldOrPropertyWithValue("code", "reconciliation.currency-mismatch")

        assertThat(events.stateOf(deliveryId)["settlement_id"]).isNull()
    }

    @Test
    fun `a second, different settlement for the delivery is a conflict`() {
        val deliveryId = UUID.randomUUID()
        events.settlement(deliveryId, expected = "70.00")

        assertThatThrownBy { events.settlement(deliveryId, expected = "80.00") }
            .isInstanceOf(ConflictException::class.java)
            .hasFieldOrPropertyWithValue("code", "reconciliation.settlement-already-applied")

        assertThat(events.stateOf(deliveryId)["expected_amount"] as BigDecimal).isEqualByComparingTo("70.00")
    }
}

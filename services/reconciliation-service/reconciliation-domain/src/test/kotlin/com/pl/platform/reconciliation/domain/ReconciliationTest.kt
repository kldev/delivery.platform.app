package com.pl.platform.reconciliation.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class ReconciliationTest {
    private val now = Instant.parse("2026-11-04T10:00:00Z")

    @Test
    fun `settlement and equal payment reconcile`() {
        val reconciliation = fromSettlement("70.00")

        reconciliation.applyPayment(UUID.randomUUID(), "TX-1", BigDecimal("70.0000"), "PLN", now)

        assertThat(reconciliation.status).isEqualTo(ReconciliationStatus.RECONCILED)
        assertThat(reconciliation.difference).isEqualByComparingTo("0")
        assertThat(reconciliation.reconciledAt).isEqualTo(now)
    }

    @Test
    fun `a payment higher than expected is a positive discrepancy`() {
        val reconciliation = fromSettlement("7.00")

        reconciliation.applyPayment(UUID.randomUUID(), "TX-1", BigDecimal("9.00"), "PLN", now)

        assertThat(reconciliation.status).isEqualTo(ReconciliationStatus.DISCREPANCY)
        assertThat(reconciliation.difference).isEqualByComparingTo("2.00")
    }

    @Test
    fun `a payment alone stays pending without an expected amount`() {
        val reconciliation = Reconciliation.fromPayment(UUID.randomUUID(), UUID.randomUUID(), "TX-1", BigDecimal("9.00"), "PLN", now)

        assertThat(reconciliation.status).isEqualTo(ReconciliationStatus.PENDING)
        assertThat(reconciliation.expectedAmount).isNull()
        assertThat(reconciliation.reconciledAt).isNull()
    }

    @Test
    fun `another currency is rejected`() {
        assertThatThrownBy { fromSettlement("7.00").applyPayment(UUID.randomUUID(), "TX-1", BigDecimal("7.00"), "EUR", now) }
            .isInstanceOf(RuleViolationException::class.java)
    }

    @Test
    fun `the same settlement again is accepted, a different one is a conflict`() {
        val settlementId = UUID.randomUUID()
        val reconciliation = Reconciliation.fromSettlement(UUID.randomUUID(), settlementId, BigDecimal("7.00"), "PLN", now)

        reconciliation.applySettlement(settlementId, BigDecimal("7.00"), "PLN", now)

        assertThatThrownBy { reconciliation.applySettlement(UUID.randomUUID(), BigDecimal("7.00"), "PLN", now) }
            .isInstanceOf(ConflictException::class.java)
    }

    private fun fromSettlement(expected: String) =
        Reconciliation.fromSettlement(UUID.randomUUID(), UUID.randomUUID(), BigDecimal(expected), "PLN", now)
}

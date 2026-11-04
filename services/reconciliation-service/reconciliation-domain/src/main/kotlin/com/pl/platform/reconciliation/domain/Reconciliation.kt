package com.pl.platform.reconciliation.domain

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Matches the delivery's price from its settlement (expected - what the customer should have paid) with what the
 * payment provider actually collected (actual), per delivery. Either side may arrive first; the status is PENDING until both are known, then
 * RECONCILED or DISCREPANCY (actual - expected).
 */
class Reconciliation private constructor(
    val id: ReconciliationId,
    val deliveryId: UUID,
    val currency: String,
    val createdAt: Instant,
    settlementId: UUID?,
    expectedAmount: BigDecimal?,
    paymentId: UUID?,
    externalTransactionId: String?,
    actualAmount: BigDecimal?,
    status: ReconciliationStatus,
    difference: BigDecimal?,
    reconciledAt: Instant?,
) {
    var settlementId = settlementId
        private set
    var expectedAmount = expectedAmount
        private set
    var paymentId = paymentId
        private set
    var externalTransactionId = externalTransactionId
        private set
    var actualAmount = actualAmount
        private set
    var status = status
        private set
    var difference = difference
        private set
    var reconciledAt = reconciledAt
        private set

    fun applySettlement(
        settlementId: UUID,
        expectedAmount: BigDecimal,
        currency: String,
        now: Instant,
    ) {
        requireSameCurrency(currency)
        val current = this.settlementId
        if (current != null && current != settlementId) {
            throw ConflictException(
                "reconciliation.settlement-already-applied",
                "Delivery $deliveryId already has settlement $current, got $settlementId",
            )
        }
        this.settlementId = settlementId
        this.expectedAmount = expectedAmount
        reconcile(now)
    }

    fun applyPayment(
        paymentId: UUID,
        externalTransactionId: String,
        actualAmount: BigDecimal,
        currency: String,
        now: Instant,
    ) {
        requireSameCurrency(currency)
        val current = this.paymentId
        if (current != null && current != paymentId) {
            throw ConflictException(
                "reconciliation.payment-already-applied",
                "Delivery $deliveryId already has payment $current, got $paymentId",
            )
        }
        this.paymentId = paymentId
        this.externalTransactionId = externalTransactionId
        this.actualAmount = actualAmount
        reconcile(now)
    }

    private fun requireSameCurrency(currency: String) {
        if (currency != this.currency) {
            throw RuleViolationException(
                "reconciliation.currency-mismatch",
                "Delivery $deliveryId is reconciled in ${this.currency}, got $currency",
            )
        }
    }

    private fun reconcile(now: Instant) {
        val expected = expectedAmount
        val actual = actualAmount
        if (expected == null || actual == null) {
            status = ReconciliationStatus.PENDING
            difference = null
            reconciledAt = null
            return
        }
        val diff = actual.subtract(expected)
        difference = diff
        status = if (diff.signum() == 0) ReconciliationStatus.RECONCILED else ReconciliationStatus.DISCREPANCY
        reconciledAt = now
    }

    companion object {
        fun fromSettlement(
            deliveryId: UUID,
            settlementId: UUID,
            expectedAmount: BigDecimal,
            currency: String,
            now: Instant,
        ) = pending(deliveryId, currency, now).apply { applySettlement(settlementId, expectedAmount, currency, now) }

        /** The payment came first: expected stays unknown (not the paid amount) until the settlement arrives. */
        fun fromPayment(
            deliveryId: UUID,
            paymentId: UUID,
            externalTransactionId: String,
            actualAmount: BigDecimal,
            currency: String,
            now: Instant,
        ) = pending(deliveryId, currency, now).apply {
            applyPayment(paymentId, externalTransactionId, actualAmount, currency, now)
        }

        fun reconstitute(
            id: ReconciliationId,
            deliveryId: UUID,
            currency: String,
            createdAt: Instant,
            settlementId: UUID?,
            expectedAmount: BigDecimal?,
            paymentId: UUID?,
            externalTransactionId: String?,
            actualAmount: BigDecimal?,
            status: ReconciliationStatus,
            difference: BigDecimal?,
            reconciledAt: Instant?,
        ) = Reconciliation(
            id, deliveryId, currency, createdAt, settlementId, expectedAmount, paymentId, externalTransactionId,
            actualAmount, status, difference, reconciledAt,
        )

        private fun pending(
            deliveryId: UUID,
            currency: String,
            now: Instant,
        ) = Reconciliation(
            id = ReconciliationId(UUID.randomUUID()),
            deliveryId = deliveryId,
            currency = currency,
            createdAt = now,
            settlementId = null,
            expectedAmount = null,
            paymentId = null,
            externalTransactionId = null,
            actualAmount = null,
            status = ReconciliationStatus.PENDING,
            difference = null,
            reconciledAt = null,
        )
    }
}

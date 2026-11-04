package com.pl.platform.reconciliation.infrastructure.persistence

import com.pl.platform.reconciliation.domain.Reconciliation
import com.pl.platform.reconciliation.domain.ReconciliationId
import com.pl.platform.reconciliation.domain.ReconciliationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "reconciliations")
class ReconciliationJpaEntity(

    @Id
    var id: UUID,

    @Column(name = "delivery_id", nullable = false)
    var deliveryId: UUID,

    @Column(name = "settlement_id") // NULL until the settlement arrives (V2__create_reconciliations.sql)
    var settlementId: UUID?,

    @Column(name = "payment_id")
    var paymentId: UUID?,

    @Column(name = "external_transaction_id")
    var externalTransactionId: String?,

    @Column(name = "expected_amount", precision = 19, scale = 4)
    var expectedAmount: BigDecimal?,

    @Column(name = "actual_amount", precision = 19, scale = 4)
    var actualAmount: BigDecimal?,

    @Column(name = "currency", nullable = false, length = 3)
    var currency: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: ReconciliationStatus,

    @Column(name = "difference", precision = 19, scale = 4)
    var difference: BigDecimal?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "reconciled_at")
    var reconciledAt: Instant?
) {
    fun updateFrom(
        reconciliation: Reconciliation
    ) {
        settlementId = reconciliation.settlementId
        paymentId = reconciliation.paymentId
        externalTransactionId = reconciliation.externalTransactionId
        expectedAmount = reconciliation.expectedAmount
        actualAmount = reconciliation.actualAmount
        difference = reconciliation.difference
        status = reconciliation.status
        reconciledAt = reconciliation.reconciledAt
    }

    fun toDomain(): Reconciliation =
        Reconciliation.reconstitute(
            id = ReconciliationId(id),
            deliveryId = deliveryId,
            currency = currency,
            createdAt = createdAt,
            settlementId = settlementId,
            expectedAmount = expectedAmount,
            paymentId = paymentId,
            externalTransactionId = externalTransactionId,
            actualAmount = actualAmount,
            status = status,
            difference = difference,
            reconciledAt = reconciledAt,
        )

    companion object {

        fun fromDomain(
            reconciliation: Reconciliation
        ) = ReconciliationJpaEntity(
            id = reconciliation.id.value,
            deliveryId = reconciliation.deliveryId,
            settlementId = reconciliation.settlementId,
            paymentId = reconciliation.paymentId,
            externalTransactionId = reconciliation.externalTransactionId,
            expectedAmount = reconciliation.expectedAmount,
            actualAmount = reconciliation.actualAmount,
            currency = reconciliation.currency,
            status = reconciliation.status,
            difference = reconciliation.difference,
            createdAt = reconciliation.createdAt,
            reconciledAt = reconciliation.reconciledAt
        )
    }
}
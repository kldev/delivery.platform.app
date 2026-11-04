package com.pl.platform.reconciliation.web.reconciliations

import com.pl.platform.reconciliation.application.port.ReconciliationDetails
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ReconciliationResponse(
    val id: UUID,
    val deliveryId: UUID,
    val settlementId: UUID?,
    val paymentId: UUID?,
    val externalTransactionId: String?,
    @field:Schema(description = "The delivery's price - what the customer should have paid - from the settlement; null until it arrives")
    val expectedAmount: BigDecimal?,
    @field:Schema(description = "What the payment provider collected; null until it arrives")
    val actualAmount: BigDecimal?,
    @field:Schema(description = "actualAmount - expectedAmount, once both are known")
    val difference: BigDecimal?,
    val currency: String,
    val status: ReconciliationStatusDto,
    val createdAt: Instant,
    val reconciledAt: Instant?,
) {
    companion object {
        fun from(details: ReconciliationDetails) =
            ReconciliationResponse(
                id = details.id,
                deliveryId = details.deliveryId,
                settlementId = details.settlementId,
                paymentId = details.paymentId,
                externalTransactionId = details.externalTransactionId,
                expectedAmount = details.expectedAmount,
                actualAmount = details.actualAmount,
                difference = details.difference,
                currency = details.currency,
                status = ReconciliationStatusDto.from(details.status),
                createdAt = details.createdAt,
                reconciledAt = details.reconciledAt,
            )
    }
}

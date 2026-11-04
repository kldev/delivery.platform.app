package com.pl.platform.reconciliation.web.reconciliations

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.reconciliation.application.getreconciliation.GetReconciliationHandler
import com.pl.platform.reconciliation.application.getreconciliation.GetReconciliationQuery
import com.pl.platform.reconciliation.application.listreconciliations.ListReconciliationsHandler
import com.pl.platform.reconciliation.application.listreconciliations.ListReconciliationsQuery
import com.pl.platform.reconciliation.application.port.PageQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/reconciliations")
@Tag(name = "Reconciliations")
class ReconciliationController(
    private val listReconciliationsHandler: ListReconciliationsHandler,
    private val getReconciliationHandler: GetReconciliationHandler,
) {
    @GetMapping
    @Operation(
        summary = "List reconciliations",
        description = "Newest first. Filter by status=DISCREPANCY to see deliveries whose payment does not match the settlement.",
    )
    fun listReconciliations(
        @RequestParam(required = false) status: ReconciliationStatusDto?,
        @RequestParam(required = false) deliveryId: UUID?,
        @RequestParam(required = false) settlementId: UUID?,
        @RequestParam(required = false) paymentId: UUID?,
        @Parameter(description = "Exact id of the transfer at the payment provider")
        @RequestParam(required = false) @Size(max = 255) externalTransactionId: String?,
        @Parameter(description = "Zero-based page number")
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @Parameter(description = "Page size, 1 to $MAX_PAGE_SIZE")
        @RequestParam(defaultValue = "100") @Min(1) @Max(MAX_PAGE_SIZE) size: Int,
    ): SliceResponse<ReconciliationResponse> {
        val slice =
            listReconciliationsHandler.handle(
                ListReconciliationsQuery(
                    status = status?.toDomain(),
                    deliveryId = deliveryId,
                    settlementId = settlementId,
                    paymentId = paymentId,
                    externalTransactionId = externalTransactionId,
                    page = PageQuery(page = page, size = size),
                ),
            )
        return SliceResponse(content = slice.content.map(ReconciliationResponse::from), hasNext = slice.hasNext)
    }

    @GetMapping("/{reconciliationId}")
    @Operation(summary = "Get a reconciliation")
    fun getReconciliation(
        @PathVariable reconciliationId: UUID,
    ): ReconciliationResponse = ReconciliationResponse.from(getReconciliationHandler.handle(GetReconciliationQuery(reconciliationId)))

    private companion object {
        const val MAX_PAGE_SIZE = 500L
    }
}

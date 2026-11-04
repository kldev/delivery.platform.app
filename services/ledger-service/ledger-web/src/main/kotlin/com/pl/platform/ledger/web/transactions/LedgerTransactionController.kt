package com.pl.platform.ledger.web.transactions

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.ledger.application.listtransactions.ListLedgerTransactionsHandler
import com.pl.platform.ledger.application.listtransactions.ListLedgerTransactionsQuery
import com.pl.platform.ledger.application.port.PageQuery
import com.pl.platform.ledger.web.shared.MAX_PAGE_SIZE
import com.pl.platform.ledger.web.shared.requireDateRange
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/ledger-transactions")
@Tag(name = "Ledger transactions")
class LedgerTransactionController(
    private val listLedgerTransactionsHandler: ListLedgerTransactionsHandler,
) {
    @GetMapping
    @Operation(
        summary = "List ledger transactions",
        description = """Newest first by occurredAt, each with its entries. `from` and `to` filter occurredAt by day
in Europe/Warsaw, both inclusive.""",
    )
    fun listLedgerTransactions(
        @Parameter(description = "Only transactions for this business object (e.g. settlement id)")
        @RequestParam(required = false) referenceId: UUID?,
        @Parameter(description = "Only transactions for this kind of business object, e.g. SETTLEMENT")
        @RequestParam(required = false) @Size(max = 50) referenceType: String?,
        @Parameter(description = "First day (yyyy-MM-dd)")
        @RequestParam(required = false) from: LocalDate?,
        @Parameter(description = "Last day (yyyy-MM-dd)")
        @RequestParam(required = false) to: LocalDate?,
        @Parameter(description = "Zero-based page number")
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @Parameter(description = "Page size, 1 to $MAX_PAGE_SIZE")
        @RequestParam(defaultValue = "100") @Min(1) @Max(MAX_PAGE_SIZE) size: Int,
    ): SliceResponse<LedgerTransactionResponse> {
        requireDateRange(from, to)
        val slice =
            listLedgerTransactionsHandler.handle(
                ListLedgerTransactionsQuery(
                    referenceId = referenceId,
                    referenceType = referenceType?.takeIf { it.isNotBlank() },
                    from = from,
                    to = to,
                    page = PageQuery(page = page, size = size),
                ),
            )
        return SliceResponse(content = slice.content.map(LedgerTransactionResponse::from), hasNext = slice.hasNext)
    }
}

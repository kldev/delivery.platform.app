package com.pl.platform.ledger.web.entries

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.ledger.application.listentries.ListLedgerEntriesHandler
import com.pl.platform.ledger.application.listentries.ListLedgerEntriesQuery
import com.pl.platform.ledger.application.port.PageQuery
import com.pl.platform.ledger.web.shared.MAX_PAGE_SIZE
import com.pl.platform.ledger.web.shared.requireDateRange
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/ledger-entries")
@Tag(name = "Ledger entries")
class LedgerEntryController(
    private val listLedgerEntriesHandler: ListLedgerEntriesHandler,
) {
    @GetMapping
    @Operation(
        summary = "List ledger entries",
        description = "Newest first. `from` and `to` are days in Europe/Warsaw, both inclusive.",
    )
    fun listLedgerEntries(
        @Parameter(description = "Only entries of this account")
        @RequestParam(required = false) accountId: UUID?,
        @Parameter(description = "Only entries of this transaction")
        @RequestParam(required = false) transactionId: UUID?,
        @Parameter(description = "First day (yyyy-MM-dd)")
        @RequestParam(required = false) from: LocalDate?,
        @Parameter(description = "Last day (yyyy-MM-dd)")
        @RequestParam(required = false) to: LocalDate?,
        @Parameter(description = "Zero-based page number")
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @Parameter(description = "Page size, 1 to $MAX_PAGE_SIZE")
        @RequestParam(defaultValue = "100") @Min(1) @Max(MAX_PAGE_SIZE) size: Int,
    ): SliceResponse<LedgerEntryResponse> {
        requireDateRange(from, to)
        val slice =
            listLedgerEntriesHandler.handle(
                ListLedgerEntriesQuery(
                    accountId = accountId,
                    transactionId = transactionId,
                    from = from,
                    to = to,
                    page = PageQuery(page = page, size = size),
                ),
            )
        return SliceResponse(content = slice.content.map(LedgerEntryResponse::from), hasNext = slice.hasNext)
    }
}

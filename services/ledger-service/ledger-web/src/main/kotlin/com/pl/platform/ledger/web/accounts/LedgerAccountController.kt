package com.pl.platform.ledger.web.accounts

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.ledger.application.getaccount.GetLedgerAccountHandler
import com.pl.platform.ledger.application.getaccount.GetLedgerAccountQuery
import com.pl.platform.ledger.application.listaccounts.ListLedgerAccountsHandler
import com.pl.platform.ledger.application.listaccounts.ListLedgerAccountsQuery
import com.pl.platform.ledger.application.port.PageQuery
import com.pl.platform.ledger.web.shared.AccountOwnerTypeDto
import com.pl.platform.ledger.web.shared.MAX_PAGE_SIZE
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/ledger-accounts")
@Tag(name = "Ledger accounts")
class LedgerAccountController(
    private val listLedgerAccountsHandler: ListLedgerAccountsHandler,
    private val getLedgerAccountHandler: GetLedgerAccountHandler,
) {
    @GetMapping
    @Operation(summary = "List ledger accounts", description = "Oldest first, with their latest computed balance.")
    fun listLedgerAccounts(
        @Parameter(description = "Only PLATFORM or DRIVER accounts")
        @RequestParam(required = false) ownerType: AccountOwnerTypeDto?,
        @Parameter(description = "Only accounts of this owner (driver id)")
        @RequestParam(required = false) ownerId: UUID?,
        @Parameter(description = "Only accounts in this currency (ISO 4217)")
        @RequestParam(required = false) @Pattern(regexp = "[A-Z]{3}") currency: String?,
        @Parameter(description = "Zero-based page number")
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @Parameter(description = "Page size, 1 to $MAX_PAGE_SIZE")
        @RequestParam(defaultValue = "100") @Min(1) @Max(MAX_PAGE_SIZE) size: Int,
    ): SliceResponse<LedgerAccountResponse> {
        val slice =
            listLedgerAccountsHandler.handle(
                ListLedgerAccountsQuery(
                    ownerType = ownerType?.toDomain(),
                    ownerId = ownerId,
                    currency = currency,
                    page = PageQuery(page = page, size = size),
                ),
            )
        return SliceResponse(content = slice.content.map(LedgerAccountResponse::from), hasNext = slice.hasNext)
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get a ledger account")
    fun getLedgerAccount(
        @PathVariable accountId: UUID,
    ): LedgerAccountResponse = LedgerAccountResponse.from(getLedgerAccountHandler.handle(GetLedgerAccountQuery(accountId)))
}

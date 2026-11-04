package com.pl.platform.ledger.web.accounts

import com.pl.platform.ledger.application.port.LedgerAccountDetails
import com.pl.platform.ledger.web.shared.AccountOwnerTypeDto
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class LedgerAccountResponse(
    val id: UUID,
    val name: String,
    val ownerType: AccountOwnerTypeDto,
    @field:Schema(description = "Driver id for DRIVER accounts, null for PLATFORM")
    val ownerId: UUID?,
    val currency: String,
    @field:Schema(description = "Credits minus debits, up to date with every posted transaction")
    val balance: BigDecimal,
    val balanceUpdatedAt: Instant?,
) {
    companion object {
        fun from(account: LedgerAccountDetails) =
            LedgerAccountResponse(
                id = account.id,
                name = account.name,
                ownerType = AccountOwnerTypeDto.from(account.ownerType),
                ownerId = account.ownerId,
                currency = account.currency,
                balance = account.balance,
                balanceUpdatedAt = account.balanceUpdatedAt,
            )
    }
}

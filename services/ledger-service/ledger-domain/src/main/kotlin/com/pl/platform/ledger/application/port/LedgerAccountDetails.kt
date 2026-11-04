package com.pl.platform.ledger.application.port

import com.pl.platform.ledger.domain.LedgerAccountOwnerType
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Read model of an account, shared by getaccount and listaccounts. [balance] (credits minus debits) comes from
 * ledger_account_balances, updated in the same transaction as every posting.
 */
data class LedgerAccountDetails(
    val id: UUID,
    val name: String,
    val ownerType: LedgerAccountOwnerType,
    val ownerId: UUID?,
    val currency: String,
    val balance: BigDecimal,
    val balanceUpdatedAt: Instant?,
)

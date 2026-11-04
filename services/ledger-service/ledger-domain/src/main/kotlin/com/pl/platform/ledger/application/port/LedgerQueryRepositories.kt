package com.pl.platform.ledger.application.port

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.ledger.application.listaccounts.ListLedgerAccountsQuery
import com.pl.platform.ledger.application.listentries.LedgerEntryItem
import com.pl.platform.ledger.application.listentries.ListLedgerEntriesQuery
import com.pl.platform.ledger.application.listtransactions.LedgerTransactionDetails
import com.pl.platform.ledger.application.listtransactions.ListLedgerTransactionsQuery
import java.util.UUID

interface LedgerAccountQueryRepository {
    fun findById(id: UUID): LedgerAccountDetails?

    /** Oldest accounts first. */
    fun search(query: ListLedgerAccountsQuery): SliceResponse<LedgerAccountDetails>
}

interface LedgerEntryQueryRepository {
    /** Newest entries first. */
    fun search(query: ListLedgerEntriesQuery): SliceResponse<LedgerEntryItem>
}

interface LedgerTransactionQueryRepository {
    /** Newest transactions (by occurredAt) first, each with its entries. */
    fun search(query: ListLedgerTransactionsQuery): SliceResponse<LedgerTransactionDetails>
}

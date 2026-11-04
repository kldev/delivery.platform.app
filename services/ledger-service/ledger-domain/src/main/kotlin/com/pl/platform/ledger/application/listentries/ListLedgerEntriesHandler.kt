package com.pl.platform.ledger.application.listentries

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.ledger.application.port.LedgerEntryQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListLedgerEntriesHandler(
    private val entries: LedgerEntryQueryRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: ListLedgerEntriesQuery): SliceResponse<LedgerEntryItem> = entries.search(query)
}

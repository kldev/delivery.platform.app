package com.pl.platform.ledger.application.listtransactions

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.ledger.application.port.LedgerTransactionQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListLedgerTransactionsHandler(
    private val transactions: LedgerTransactionQueryRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: ListLedgerTransactionsQuery): SliceResponse<LedgerTransactionDetails> = transactions.search(query)
}

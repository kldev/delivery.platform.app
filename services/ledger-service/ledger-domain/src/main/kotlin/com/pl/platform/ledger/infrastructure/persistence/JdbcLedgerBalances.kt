package com.pl.platform.ledger.infrastructure.persistence

import com.pl.platform.ledger.application.port.LedgerBalances
import com.pl.platform.ledger.domain.LedgerTransaction
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

/**
 * Adds each entry to its account's balance row in the posting transaction, so balances are never stale.
 * Rows are updated in account id order: two postings touching the same accounts lock them in the same order
 * and cannot deadlock. The platform account row serializes concurrent postings of one currency, which is the
 * price of an always-correct balance.
 */
@Repository
class JdbcLedgerBalances(
    private val jdbc: JdbcTemplate,
) : LedgerBalances {
    override fun apply(transaction: LedgerTransaction) {
        transaction.entries
            .groupBy { it.accountId }
            .mapValues { (_, entries) -> entries.sumOf { it.signedAmount() } }
            .toSortedMap()
            .forEach { (accountId, delta) ->
                jdbc.update(
                    """
                    INSERT INTO ledger_account_balances (account_id, currency, balance, updated_at)
                    VALUES (?, ?, ?, now())
                    ON CONFLICT (account_id)
                    DO UPDATE SET balance = ledger_account_balances.balance + EXCLUDED.balance, updated_at = now()
                    """.trimIndent(),
                    accountId,
                    transaction.currency,
                    delta,
                )
            }
    }
}

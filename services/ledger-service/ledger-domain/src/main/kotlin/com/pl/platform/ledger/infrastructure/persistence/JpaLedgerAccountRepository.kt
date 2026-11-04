package com.pl.platform.ledger.infrastructure.persistence

import com.pl.platform.ledger.application.port.LedgerAccountRepository
import com.pl.platform.ledger.domain.LedgerAccount
import com.pl.platform.ledger.domain.LedgerAccountOwnerType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JpaLedgerAccountRepository(
    private val repository: SpringDataLedgerAccountRepository,
    private val jdbc: JdbcTemplate,
) : LedgerAccountRepository {

    override fun findByOwnerAndCurrency(
        ownerType: LedgerAccountOwnerType,
        ownerId: UUID?,
        currency: String,
    ): LedgerAccount? =
        repository
            .findByOwnerTypeAndOwnerIdAndCurrency(
                ownerType = ownerType,
                ownerId = ownerId,
                currency = currency,
            )
            ?.toDomain()

    // A new account starts with a zero balance row, so every account has a balance.
    override fun save(
        account: LedgerAccount,
    ): LedgerAccount {
        val saved = repository.saveAndFlush(account.toJpaEntity()).toDomain()
        jdbc.update(
            """
            INSERT INTO ledger_account_balances (account_id, currency, balance, updated_at)
            VALUES (?, ?, 0, now())
            ON CONFLICT (account_id) DO NOTHING
            """.trimIndent(),
            saved.id,
            saved.currency,
        )
        return saved
    }
}

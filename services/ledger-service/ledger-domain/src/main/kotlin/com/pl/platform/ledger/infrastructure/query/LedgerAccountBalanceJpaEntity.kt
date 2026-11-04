package com.pl.platform.ledger.infrastructure.query

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ledger_account_balances")
class LedgerAccountBalanceJpaEntity(

    @Id
    @Column(name = "account_id")
    var accountId: UUID,

    @Column(name = "currency", nullable = false, length = 3)
    var currency: String,

    @Column(
        name = "balance",
        nullable = false,
        precision = 19,
        scale = 4,
    )
    var balance: BigDecimal,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
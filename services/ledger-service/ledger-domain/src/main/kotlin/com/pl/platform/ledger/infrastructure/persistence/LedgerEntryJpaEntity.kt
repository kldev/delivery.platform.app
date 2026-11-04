package com.pl.platform.ledger.infrastructure.persistence

import com.pl.platform.ledger.domain.LedgerEntryType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(name = "ledger_entries")
class LedgerEntryJpaEntity(

    @Id
    var id: UUID,

    @Column(name = "account_id", nullable = false)
    var accountId: UUID,

    @Column(
        name = "amount",
        nullable = false,
        precision = 19,
        scale = 4,
    )
    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(
        name = "type",
        length = 30
    )
    var type: LedgerEntryType,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    ) {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "transaction_id",
        nullable = false,
    )
    lateinit var transaction: LedgerTransactionJpaEntity

    // Same column as the relation, read-only: lets queries filter and map by transaction without loading it.
    @Column(name = "transaction_id", insertable = false, updatable = false)
    var transactionId: UUID? = null

    fun signedAmount(): BigDecimal =
        when (type) {
            LedgerEntryType.DEBIT -> amount.negate()
            LedgerEntryType.CREDIT -> amount
        }
}
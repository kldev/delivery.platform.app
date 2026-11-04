package com.pl.platform.ledger.infrastructure.persistence

import com.pl.platform.ledger.domain.LedgerTransactionType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "ledger_transactions",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_ledger_transactions_reference",
            columnNames = ["reference_type", "reference_id"],
        ),
    ],
)
class LedgerTransactionJpaEntity(

    @Id
    var id: UUID,

    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    var type: LedgerTransactionType,

    @Column(name = "reference_type", nullable = false, length = 50)
    var referenceType: String,

    @Column(name = "reference_id", nullable = false)
    var referenceId: UUID,

    @Column(name = "currency", nullable = false, length = 3)
    var currency: String,

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
)
{
    @OneToMany(
        mappedBy = "transaction",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    var entries: MutableList<LedgerEntryJpaEntity> = mutableListOf()


}
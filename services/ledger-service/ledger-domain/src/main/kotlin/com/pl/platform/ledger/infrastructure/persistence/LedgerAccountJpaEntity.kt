package com.pl.platform.ledger.infrastructure.persistence

import com.pl.platform.ledger.domain.LedgerAccountOwnerType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "ledger_accounts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "ux_ledger_accounts_owner_currency",
            columnNames = ["owner_type", "owner_id", "currency"],
        ),
    ],
)
class LedgerAccountJpaEntity(

    @Id
    var id: UUID,

    @Column(name = "owner_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    var ownerType: LedgerAccountOwnerType,

    @Column(name = "owner_id")
    var ownerId: UUID?,

    @Column(name = "currency", nullable = false, length = 3)
    var currency: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "name", nullable = false, length = 200)
    var name: String,
)
package com.pl.platform.ledger.infrastructure.query

import com.pl.platform.ledger.application.listaccounts.ListLedgerAccountsQuery
import com.pl.platform.ledger.domain.LedgerAccountOwnerType
import com.pl.platform.ledger.infrastructure.persistence.LedgerAccountJpaEntity
import org.springframework.data.jpa.domain.Specification
import java.util.UUID

object LedgerAccountSpecifications {
    fun build(query: ListLedgerAccountsQuery): Specification<LedgerAccountJpaEntity> =
        listOfNotNull(
            query.ownerType?.let { type ->
                Specification<LedgerAccountJpaEntity> { root, _, cb ->
                    cb.equal(root.get<LedgerAccountOwnerType>("ownerType"), type)
                }
            },
            query.ownerId?.let { id ->
                Specification<LedgerAccountJpaEntity> { root, _, cb -> cb.equal(root.get<UUID>("ownerId"), id) }
            },
            query.currency?.let { currency ->
                Specification<LedgerAccountJpaEntity> { root, _, cb -> cb.equal(root.get<String>("currency"), currency) }
            },
        ).fold(Specification { _, _, _ -> null }) { result, specification -> result.and(specification) }
}

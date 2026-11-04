package com.pl.platform.ledger.infrastructure.query

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.ledger.application.listentries.LedgerEntryItem
import com.pl.platform.ledger.application.listentries.ListLedgerEntriesQuery
import com.pl.platform.ledger.application.port.LedgerEntryQueryRepository
import com.pl.platform.ledger.infrastructure.persistence.LedgerEntryJpaEntity
import jakarta.persistence.EntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component

@Component
class JpaLedgerEntryQueryRepository(
    entityManager: EntityManager,
) : AbstractJpaSliceQueryRepository<LedgerEntryJpaEntity, ListLedgerEntriesQuery, LedgerEntryItem>(entityManager),
    LedgerEntryQueryRepository {
    override fun search(query: ListLedgerEntriesQuery): SliceResponse<LedgerEntryItem> =
        searchSlice(
            query,
            PageRequest.of(query.page.page, query.page.size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
        )

    override fun entityType(): Class<LedgerEntryJpaEntity> = LedgerEntryJpaEntity::class.java

    override fun specification(query: ListLedgerEntriesQuery): Specification<LedgerEntryJpaEntity> =
        LedgerEntrySpecifications.build(query)

    override fun from(entity: LedgerEntryJpaEntity): LedgerEntryItem =
        LedgerEntryItem(
            id = entity.id,
            transactionId = checkNotNull(entity.transactionId) { "Entry ${entity.id} read without its transaction_id" },
            accountId = entity.accountId,
            amount = entity.amount,
            type = entity.type,
            createdAt = entity.createdAt,
        )
}

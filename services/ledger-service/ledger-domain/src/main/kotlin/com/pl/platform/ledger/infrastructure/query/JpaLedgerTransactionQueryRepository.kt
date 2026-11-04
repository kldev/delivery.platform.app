package com.pl.platform.ledger.infrastructure.query

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.ledger.application.listtransactions.LedgerTransactionDetails
import com.pl.platform.ledger.application.listtransactions.ListLedgerTransactionsQuery
import com.pl.platform.ledger.application.port.LedgerTransactionQueryRepository
import com.pl.platform.ledger.infrastructure.persistence.LedgerEntryJpaEntity
import com.pl.platform.ledger.infrastructure.persistence.LedgerTransactionJpaEntity
import jakarta.persistence.EntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component

@Component
class JpaLedgerTransactionQueryRepository(
    entityManager: EntityManager,
) : AbstractJpaSliceQueryRepository<LedgerTransactionJpaEntity, ListLedgerTransactionsQuery, LedgerTransactionDetails>(
        entityManager,
    ),
    LedgerTransactionQueryRepository {
    // Sorted by occurredAt, the same field the from/to filter uses; id breaks ties.
    override fun search(query: ListLedgerTransactionsQuery): SliceResponse<LedgerTransactionDetails> =
        searchSlice(
            query,
            PageRequest.of(query.page.page, query.page.size, Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("id"))),
        )

    override fun entityType(): Class<LedgerTransactionJpaEntity> = LedgerTransactionJpaEntity::class.java

    override fun specification(query: ListLedgerTransactionsQuery): Specification<LedgerTransactionJpaEntity> =
        LedgerTransactionSpecifications.build(query)

    // Entries of the whole page in one query, instead of a lazy load per transaction.
    override fun mapAll(entities: List<LedgerTransactionJpaEntity>): List<LedgerTransactionDetails> {
        if (entities.isEmpty()) return emptyList()
        val entriesByTransaction =
            entityManager
                .createQuery(
                    "SELECT e FROM LedgerEntryJpaEntity e WHERE e.transactionId IN :ids ORDER BY e.type, e.id",
                    LedgerEntryJpaEntity::class.java,
                ).setParameter("ids", entities.map { it.id })
                .resultList
                .groupBy { it.transactionId }
        return entities.map { transaction ->
            LedgerTransactionDetails(
                id = transaction.id,
                type = transaction.type,
                referenceType = transaction.referenceType,
                referenceId = transaction.referenceId,
                currency = transaction.currency,
                occurredAt = transaction.occurredAt,
                createdAt = transaction.createdAt,
                entries =
                    entriesByTransaction[transaction.id].orEmpty().map {
                        LedgerTransactionDetails.Entry(id = it.id, accountId = it.accountId, amount = it.amount, type = it.type)
                    },
            )
        }
    }

    override fun from(entity: LedgerTransactionJpaEntity): LedgerTransactionDetails = mapAll(listOf(entity)).single()
}

package com.pl.platform.ledger.infrastructure.query

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.ledger.application.listaccounts.ListLedgerAccountsQuery
import com.pl.platform.ledger.application.port.LedgerAccountDetails
import com.pl.platform.ledger.application.port.LedgerAccountQueryRepository
import com.pl.platform.ledger.infrastructure.persistence.LedgerAccountJpaEntity
import jakarta.persistence.EntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
class JpaLedgerAccountQueryRepository(
    entityManager: EntityManager,
    private val balances: SpringDataLedgerAccountBalanceRepository,
) : AbstractJpaSliceQueryRepository<LedgerAccountJpaEntity, ListLedgerAccountsQuery, LedgerAccountDetails>(entityManager),
    LedgerAccountQueryRepository {
    override fun findById(id: UUID): LedgerAccountDetails? =
        entityManager.find(LedgerAccountJpaEntity::class.java, id)?.let { mapAll(listOf(it)).single() }

    override fun search(query: ListLedgerAccountsQuery): SliceResponse<LedgerAccountDetails> =
        searchSlice(
            query,
            PageRequest.of(query.page.page, query.page.size, Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"))),
        )

    override fun entityType(): Class<LedgerAccountJpaEntity> = LedgerAccountJpaEntity::class.java

    override fun specification(query: ListLedgerAccountsQuery): Specification<LedgerAccountJpaEntity> =
        LedgerAccountSpecifications.build(query)

    // Balances of the whole page in one query.
    override fun mapAll(entities: List<LedgerAccountJpaEntity>): List<LedgerAccountDetails> {
        val balanceById = balances.findAllById(entities.map { it.id }).associateBy { it.accountId }
        return entities.map { account ->
            val balance = balanceById[account.id]
            LedgerAccountDetails(
                id = account.id,
                name = account.name,
                ownerType = account.ownerType,
                ownerId = account.ownerId,
                currency = account.currency,
                // Every account gets a balance row on creation; ZERO only covers rows missing from before V10.
                balance = balance?.balance ?: BigDecimal.ZERO,
                balanceUpdatedAt = balance?.updatedAt,
            )
        }
    }

    override fun from(entity: LedgerAccountJpaEntity): LedgerAccountDetails = mapAll(listOf(entity)).single()
}

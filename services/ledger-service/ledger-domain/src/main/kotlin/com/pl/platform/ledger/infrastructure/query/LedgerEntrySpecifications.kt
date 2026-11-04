package com.pl.platform.ledger.infrastructure.query

import com.pl.platform.ledger.application.listentries.ListLedgerEntriesQuery
import com.pl.platform.ledger.infrastructure.persistence.LedgerEntryJpaEntity
import org.springframework.data.jpa.domain.Specification
import java.time.Instant
import java.util.UUID

object LedgerEntrySpecifications {

    fun build(
        query: ListLedgerEntriesQuery,
    ): Specification<LedgerEntryJpaEntity> {

        return listOfNotNull(
            query.accountId
                ?.let(::accountIdEquals),

            query.transactionId
                ?.let(::transactionIdEquals),

            query.fromAtInstant()
                ?.let(::createdAtGreaterThanOrEqual),

            query.toAtInstant()
                ?.let(::createdAtLessThan),
        ).fold(
            Specification { _, _, _ -> null }
        ) { result, specification ->
            result.and(specification)
        }
    }

    fun accountIdEquals(
        accountId: UUID,
    ): Specification<LedgerEntryJpaEntity> =
        Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<UUID>("accountId"),
                accountId,
            )
        }

    fun transactionIdEquals(
        transactionId: UUID,
    ): Specification<LedgerEntryJpaEntity> =
        Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<UUID>("transactionId"),
                transactionId,
            )
        }


    fun createdAtGreaterThanOrEqual(
        from: Instant,
    ): Specification<LedgerEntryJpaEntity> =
        Specification { root, _, criteriaBuilder ->
            criteriaBuilder.greaterThanOrEqualTo(
                root.get("createdAt"),
                from,
            )
        }

    fun createdAtLessThan(
        to: Instant,
    ): Specification<LedgerEntryJpaEntity> =
        Specification { root, _, criteriaBuilder ->
            criteriaBuilder.lessThan(
                root.get("createdAt"),
                to,
            )
        }

}
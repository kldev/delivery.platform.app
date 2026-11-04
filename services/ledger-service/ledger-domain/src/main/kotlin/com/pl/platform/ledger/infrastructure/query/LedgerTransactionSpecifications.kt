package com.pl.platform.ledger.infrastructure.query

import com.pl.platform.ledger.application.listtransactions.ListLedgerTransactionsQuery
import com.pl.platform.ledger.infrastructure.persistence.LedgerTransactionJpaEntity
import org.springframework.data.jpa.domain.Specification
import java.time.Instant
import java.util.UUID

object LedgerTransactionSpecifications {

    fun build(
        query: ListLedgerTransactionsQuery,
    ): Specification<LedgerTransactionJpaEntity> {

        return listOfNotNull(
            query.referenceId
                ?.let(::referenceIdEquals),

            query.referenceType
                ?.takeIf { it.isNotBlank() }
                ?.let(::referenceTypeEquals),

            query.fromAtInstant()
                ?.let(::occurredAtGreaterThanOrEqual),

            query.toAtInstant()
                ?.let(::occurredAtLessThan),
        ).fold(
            Specification { _, _, _ -> null }
        ) { result, specification ->
            result.and(specification)
        }
    }

    fun referenceIdEquals(
        referenceId: UUID,
    ): Specification<LedgerTransactionJpaEntity> =
        Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<UUID>("referenceId"),
                referenceId,
            )
        }

    fun referenceTypeEquals(
        referenceType: String,
    ): Specification<LedgerTransactionJpaEntity> =
        Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<String>("referenceType"),
                referenceType,
            )
        }

    fun occurredAtGreaterThanOrEqual(
        from: Instant,
    ): Specification<LedgerTransactionJpaEntity> =
        Specification { root, _, criteriaBuilder ->
            criteriaBuilder.greaterThanOrEqualTo(
                root.get("occurredAt"),
                from,
            )
        }

    fun occurredAtLessThan(
        to: Instant,
    ): Specification<LedgerTransactionJpaEntity> =
        Specification { root, _, criteriaBuilder ->
            criteriaBuilder.lessThan(
                root.get("occurredAt"),
                to,
            )
        }
}
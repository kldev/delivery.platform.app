package com.pl.platform.ledger.infrastructure.persistence

import com.pl.platform.ledger.domain.LedgerTransaction

fun LedgerTransactionJpaEntity.toDomain(): LedgerTransaction =
    LedgerTransaction.create(
        id = id,
        type = type,
        referenceType = referenceType,
        referenceId = referenceId,
        currency = currency,
        occurredAt = occurredAt,
        entries = entries.map { it.toDomain() },
    )

fun LedgerTransaction.toJpaEntity(): LedgerTransactionJpaEntity {
    val entity = LedgerTransactionJpaEntity(
        id = id,
        type = type,
        referenceType = referenceType,
        referenceId = referenceId,
        currency = currency,
        occurredAt = occurredAt,
    )

    entity.entries.addAll(
        entries.map { it ->
            LedgerEntryJpaEntity(
                id = it.id,
                accountId = it.accountId,
                amount = it.amount,
                type = it.type,
            ).also {
                it.transaction = entity
            }
        }
    )

    return entity
}
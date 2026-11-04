package com.pl.platform.ledger.domain

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class LedgerTransaction private constructor(
    val id: UUID,
    val type: LedgerTransactionType,
    val referenceType: String,
    val referenceId: UUID,
    val currency: String,
    val occurredAt: Instant,
    val entries: List<LedgerEntry>,
) {

    init {
        require(referenceType.isNotBlank()) {
            "Reference type must not be blank"
        }

        require(currency.matches(Regex("[A-Z]{3}"))) {
            "Currency must be a valid ISO 4217 code"
        }

        require(entries.isNotEmpty()) {
            "Ledger transaction must contain at least one entry"
        }

        require(entries.sumOf { it.signedAmount() }.compareTo(BigDecimal.ZERO) == 0) {
            "Ledger transaction must be balanced"
        }

        require(entries.map { it.accountId }.distinct().size == entries.size) {
            "Ledger transaction cannot contain multiple entries for the same account"
        }
    }

    companion object {

        fun create(
            id: UUID,
            type: LedgerTransactionType,
            referenceType: String,
            referenceId: UUID,
            currency: String,
            occurredAt: Instant,
            entries: List<LedgerEntry>,
        ): LedgerTransaction =
            LedgerTransaction(
                id = id,
                type = type,
                referenceType = referenceType,
                referenceId = referenceId,
                currency = currency,
                occurredAt = occurredAt,
                entries = entries.toList(),
            )

        fun driverSettlement(
            id: UUID,
            settlementId: UUID,
            currency: String,
            amount: BigDecimal,
            platformAccountId: UUID,
            driverAccountId: UUID,
            occurredAt: Instant,
        ): LedgerTransaction {

            require(amount > BigDecimal.ZERO) {
                "Settlement amount must be greater than zero"
            }

            return create(
                id = id,
                type = LedgerTransactionType.DRIVER_SETTLEMENT,
                referenceType = "SETTLEMENT",
                referenceId = settlementId,
                currency = currency,
                occurredAt = occurredAt,
                // The platform owes the driver: debit the platform, credit the driver. Balances are
                // credits minus debits, so the driver's balance grows by what the platform owes them.
                entries = listOf(
                    LedgerEntry(
                        id = UUID.randomUUID(),
                        accountId = platformAccountId,
                        amount = amount.abs(),
                        type = LedgerEntryType.DEBIT
                    ),
                    LedgerEntry(
                        id = UUID.randomUUID(),
                        accountId = driverAccountId,
                        amount = amount.abs(),
                        type = LedgerEntryType.CREDIT
                    ),
                ),
            )
        }
    }
}
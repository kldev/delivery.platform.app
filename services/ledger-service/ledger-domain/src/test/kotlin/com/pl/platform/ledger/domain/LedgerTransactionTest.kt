package com.pl.platform.ledger.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class LedgerTransactionTest {
    private val platform = UUID.randomUUID()
    private val driver = UUID.randomUUID()

    @Test
    fun `a driver settlement debits the platform and credits the driver by the same amount`() {
        val transaction = settlement(BigDecimal("125.50"))

        assertThat(transaction.type).isEqualTo(LedgerTransactionType.DRIVER_SETTLEMENT)
        assertThat(transaction.referenceType).isEqualTo("SETTLEMENT")
        assertThat(transaction.entries.single { it.accountId == platform }.type).isEqualTo(LedgerEntryType.DEBIT)
        assertThat(transaction.entries.single { it.accountId == driver }.type).isEqualTo(LedgerEntryType.CREDIT)
        assertThat(transaction.entries.sumOf { it.signedAmount() }).isEqualByComparingTo("0")
    }

    @Test
    fun `a settlement of zero or less is rejected`() {
        assertThatThrownBy { settlement(BigDecimal.ZERO) }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { settlement(BigDecimal("-1")) }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `an unbalanced transaction is rejected`() {
        assertThatThrownBy {
            transaction(
                LedgerEntry(UUID.randomUUID(), platform, BigDecimal("10"), LedgerEntryType.CREDIT),
                LedgerEntry(UUID.randomUUID(), driver, BigDecimal("9"), LedgerEntryType.DEBIT),
            )
        }.hasMessageContaining("balanced")
    }

    @Test
    fun `two entries on the same account are rejected`() {
        assertThatThrownBy {
            transaction(
                LedgerEntry(UUID.randomUUID(), platform, BigDecimal("10"), LedgerEntryType.CREDIT),
                LedgerEntry(UUID.randomUUID(), platform, BigDecimal("10"), LedgerEntryType.DEBIT),
            )
        }.hasMessageContaining("same account")
    }

    @Test
    fun `an account must use an ISO currency code`() {
        assertThatThrownBy { LedgerAccount.platform(UUID.randomUUID(), "Platform", "pln") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    private fun settlement(amount: BigDecimal) =
        LedgerTransaction.driverSettlement(
            id = UUID.randomUUID(),
            settlementId = UUID.randomUUID(),
            currency = "PLN",
            amount = amount,
            platformAccountId = platform,
            driverAccountId = driver,
            occurredAt = Instant.parse("2026-08-23T10:00:00Z"),
        )

    private fun transaction(vararg entries: LedgerEntry) =
        LedgerTransaction.create(
            id = UUID.randomUUID(),
            type = LedgerTransactionType.ADJUSTMENT,
            referenceType = "TEST",
            referenceId = UUID.randomUUID(),
            currency = "PLN",
            occurredAt = Instant.now(),
            entries = entries.toList(),
        )
}

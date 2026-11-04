package com.pl.platform.ledger.testing

import com.pl.platform.ledger.application.port.LedgerAccountRepository
import com.pl.platform.ledger.application.port.LedgerBalances
import com.pl.platform.ledger.application.port.LedgerTransactionRepository
import com.pl.platform.ledger.domain.LedgerAccount
import com.pl.platform.ledger.domain.LedgerTransaction
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/** Builds ledger data through the domain and the ports, not through JPA entities. */
@Component
class LedgerFixture(
    private val accounts: LedgerAccountRepository,
    private val transactions: LedgerTransactionRepository,
    private val balances: LedgerBalances,
) {
    fun platformAccount(currency: String = "PLN"): LedgerAccount =
        accounts.save(LedgerAccount.platform(id = UUID.randomUUID(), name = "Platform $currency", currency = currency))

    fun driverAccount(
        driverId: UUID = UUID.randomUUID(),
        currency: String = "PLN",
    ): LedgerAccount =
        accounts.save(
            LedgerAccount.driver(id = UUID.randomUUID(), name = "Driver $driverId", driverId = driverId, currency = currency),
        )

    fun settlement(
        platform: LedgerAccount,
        driver: LedgerAccount,
        amount: BigDecimal = BigDecimal("100.00"),
        occurredAt: Instant = Instant.parse("2026-08-23T10:00:00Z"),
        settlementId: UUID = UUID.randomUUID(),
    ): LedgerTransaction =
        transactions
            .save(
                LedgerTransaction.driverSettlement(
                id = UUID.randomUUID(),
                settlementId = settlementId,
                currency = platform.currency,
                amount = amount,
                platformAccountId = platform.id,
                driverAccountId = driver.id,
                occurredAt = occurredAt,
                ),
            ).also(balances::apply)
}

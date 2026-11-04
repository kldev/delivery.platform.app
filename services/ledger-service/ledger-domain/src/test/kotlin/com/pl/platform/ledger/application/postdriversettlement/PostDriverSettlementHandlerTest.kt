package com.pl.platform.ledger.application.postdriversettlement

import com.pl.platform.ledger.testing.BaseIntegrationTest
import com.pl.platform.ledger.domain.LedgerAccountOwnerType
import com.pl.platform.ledger.domain.LedgerEntryType
import com.pl.platform.ledger.infrastructure.persistence.LedgerAccountJpaEntity
import com.pl.platform.ledger.infrastructure.persistence.SpringDataLedgerAccountRepository
import com.pl.platform.ledger.infrastructure.persistence.SpringDataLedgerTransactionRepository
import com.pl.platform.ledger.infrastructure.query.SpringDataLedgerAccountBalanceRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PostDriverSettlementHandlerTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var handler: PostDriverSettlementHandler

    @Autowired
    private lateinit var ledgerAccountRepository: SpringDataLedgerAccountRepository

    @Autowired
    private lateinit var ledgerTransactionRepository: SpringDataLedgerTransactionRepository

    @Autowired
    private lateinit var ledgerAccountBalanceRepository: SpringDataLedgerAccountBalanceRepository

    @BeforeEach
    fun setUp() {
        cleanDatabase()
    }

    @Test
    fun `should create driver account and post settlement`() {
        val driverId = UUID.randomUUID()
        val settlementId = UUID.randomUUID()

        val platformAccount = createPlatformAccount()

        val command = command(
            settlementId = settlementId,
            driverId = driverId,
            amount = BigDecimal("125.50"),
            currency = "PLN"
        )

        handler.handle(command)

        val driverAccount =
            ledgerAccountRepository.findByOwnerTypeAndOwnerIdAndCurrency(
                ownerType = LedgerAccountOwnerType.DRIVER,
                ownerId = driverId,
                currency = "PLN",
            )

        assertNotNull(driverAccount)

        assertEquals(
            driverId,
            driverAccount.ownerId,
        )

        assertEquals(
            LedgerAccountOwnerType.DRIVER,
            driverAccount.ownerType,
        )

        assertEquals(
            "DRIVER:$driverId",
            driverAccount.name,
        )

        val transaction =
            ledgerTransactionRepository
                .findByReferenceTypeAndReferenceIdWithEntries(
                    referenceType = "SETTLEMENT",
                    referenceId = settlementId,
                )

        assertNotNull(transaction)

        assertEquals(
            2,
            transaction.entries.size,
        )

        val platformEntry =
            transaction.entries.first {
                it.accountId == platformAccount.id
            }

        val driverEntry =
            transaction.entries.first {
                it.accountId == driverAccount.id
            }

        assertAmount(
            BigDecimal("125.50"),
            platformEntry.amount,
        )

        assertAmount(
            BigDecimal("125.50"),
            driverEntry.amount,
        )

        assertEquals(
            0,
            transaction.entries
                .sumOf { it.signedAmount() }
                .compareTo(BigDecimal.ZERO),
        )
    }

    @Test
    fun `should use existing driver account when posting settlement`() {
        val driverId = UUID.randomUUID()
        val settlementId = UUID.randomUUID()

        createPlatformAccount()

        val existingDriver =
            createDriverAccount(driverId)

        handler.handle(
            command(
                settlementId = settlementId,
                driverId = driverId,
                amount = BigDecimal("100.00"),

            )
        )

        val driverAccounts =
            ledgerAccountRepository.findAll()
                .filter {
                    it.ownerType == LedgerAccountOwnerType.DRIVER &&
                            it.ownerId == driverId &&
                            it.currency == "PLN"
                }

        assertEquals(1, driverAccounts.size)

        assertEquals(
            existingDriver.id,
            driverAccounts.single().id,
        )

        assertEquals(
            1,
            ledgerTransactionRepository.count(),
        )
    }

    @Test
    fun `should not create duplicate transaction when settlement is processed twice`() {
        val driverId = UUID.randomUUID()
        val settlementId = UUID.randomUUID()

        createPlatformAccount()

        val command =
            command(
                settlementId = settlementId,
                driverId = driverId,
                amount = BigDecimal("100.00"),
            )

        handler.handle(command)
        handler.handle(command)

        assertEquals(
            1,
            ledgerTransactionRepository.count(),
        )

        assertEquals(
            1,
            ledgerAccountRepository.findAll()
                .count {
                    it.ownerType == LedgerAccountOwnerType.DRIVER &&
                            it.ownerId == driverId
                },
        )

        val transaction =
            ledgerTransactionRepository
                .findByReferenceTypeAndReferenceIdWithEntries(
                    referenceType = "SETTLEMENT",
                    referenceId = settlementId,
                )

        assertNotNull(transaction)

        assertEquals(
            2,
            transaction.entries.size,
        )
    }

    @Test
    fun `should fail when platform account does not exist`() {
        val driverId = UUID.randomUUID()
        val settlementId = UUID.randomUUID()

        val exception =
            kotlin.runCatching {
                handler.handle(
                    command(
                        settlementId = settlementId,
                        driverId = driverId,
                        amount = BigDecimal("100.00"),
                        currency = "PLN"
                    )
                )
            }.exceptionOrNull()

        assertNotNull(exception)

        assertEquals(
            "Platform ledger account not found for currency PLN",
            exception.message,
        )

        assertEquals(
            0,
            ledgerAccountRepository.count(),
        )

        assertEquals(
            0,
            ledgerTransactionRepository.count(),
        )
    }

    @Test
    fun `should create separate transactions for different settlements`() {
        val driverId = UUID.randomUUID()

        createPlatformAccount()

        val firstSettlementId = UUID.randomUUID()
        val secondSettlementId = UUID.randomUUID()

        handler.handle(
            command(
                settlementId = firstSettlementId,
                driverId = driverId,
                amount = BigDecimal("100.00"),
            )
        )

        handler.handle(
            command(
                settlementId = secondSettlementId,
                driverId = driverId,
                amount = BigDecimal("150.00"),
            )
        )

        assertEquals(
            2,
            ledgerTransactionRepository.count(),
        )

        assertEquals(
            1,
            ledgerAccountRepository.findAll()
                .count {
                    it.ownerType == LedgerAccountOwnerType.DRIVER &&
                            it.ownerId == driverId
                },
        )

        val firstTransaction =
            ledgerTransactionRepository
                .findByReferenceTypeAndReferenceIdWithEntries(
                    "SETTLEMENT",
                    firstSettlementId,
                )

        val secondTransaction =
            ledgerTransactionRepository
                .findByReferenceTypeAndReferenceIdWithEntries(
                    "SETTLEMENT",
                    secondSettlementId,
                )

        assertNotNull(firstTransaction)
        assertNotNull(secondTransaction)

        assertEquals(
            0,
            firstTransaction.entries
                .sumOf { it.signedAmount() }
                .compareTo(BigDecimal.ZERO),
        )

        assertEquals(
            0,
            secondTransaction.entries
                .sumOf { it.signedAmount() }
                .compareTo(BigDecimal.ZERO),
        )
    }

    @Test
    fun `the same event delivered twice is posted once and marked as processed`() {
        createPlatformAccount()
        val command = command(settlementId = UUID.randomUUID(), driverId = UUID.randomUUID(), amount = BigDecimal("80.00"))

        handler.handle(command)
        handler.handle(command)

        assertEquals(1, ledgerTransactionRepository.count())
        assertEquals(
            1,
            jdbcTemplate.queryForObject("SELECT count(*) FROM processed_events WHERE event_id = ?", Long::class.java, command.eventId),
        )
    }

    @Test
    fun `a failed event is not marked as processed, so a redelivery can post it`() {
        val command = command(settlementId = UUID.randomUUID(), driverId = UUID.randomUUID(), amount = BigDecimal("80.00"))

        runCatching { handler.handle(command) } // no platform account yet

        assertEquals(
            0,
            jdbcTemplate.queryForObject("SELECT count(*) FROM processed_events WHERE event_id = ?", Long::class.java, command.eventId),
        )

        createPlatformAccount()
        handler.handle(command)

        assertEquals(1, ledgerTransactionRepository.count())
    }

    @Test
    fun `posting moves the balances at once - platform down, driver up - and they add up across settlements`() {
        val platformAccount = createPlatformAccount()
        val driverId = UUID.randomUUID()

        handler.handle(command(settlementId = UUID.randomUUID(), driverId = driverId, amount = BigDecimal("100.00")))
        handler.handle(command(settlementId = UUID.randomUUID(), driverId = driverId, amount = BigDecimal("25.50")))

        val driverAccount =
            checkNotNull(ledgerAccountRepository.findByOwnerTypeAndOwnerIdAndCurrency(LedgerAccountOwnerType.DRIVER, driverId, "PLN"))
        assertAmount(BigDecimal("125.50"), balanceOf(driverAccount.id))
        assertAmount(BigDecimal("-125.50"), balanceOf(platformAccount.id))
    }

    @Test
    fun `the driver entry is a credit and the platform entry a debit`() {
        val platformAccount = createPlatformAccount()
        val settlementId = UUID.randomUUID()

        handler.handle(command(settlementId = settlementId, driverId = UUID.randomUUID(), amount = BigDecimal("10.00")))

        val entries = checkNotNull(ledgerTransactionRepository.findByReferenceTypeAndReferenceIdWithEntries("SETTLEMENT", settlementId)).entries
        assertEquals(LedgerEntryType.DEBIT, entries.single { it.accountId == platformAccount.id }.type)
        assertEquals(LedgerEntryType.CREDIT, entries.single { it.accountId != platformAccount.id }.type)
    }

    private fun balanceOf(accountId: UUID): BigDecimal =
        jdbcTemplate.queryForObject("SELECT balance FROM ledger_account_balances WHERE account_id = ?", BigDecimal::class.java, accountId)!!

    private fun createPlatformAccount(
        currency: String = "PLN",
    ) =
        ledgerAccountRepository.save(
            LedgerAccountJpaEntity(
                id = UUID.randomUUID(),
                name = "Platform $currency",
                ownerType = LedgerAccountOwnerType.PLATFORM,
                ownerId = null,
                currency = currency,
            )
        )

    private fun createDriverAccount(
        driverId: UUID,
        currency: String = "PLN",
    ) =
        ledgerAccountRepository.save(
            LedgerAccountJpaEntity(
                id = UUID.randomUUID(),
                name = "DRIVER:$driverId",
                ownerType = LedgerAccountOwnerType.DRIVER,
                ownerId = driverId,
                currency = currency,
            )
        )

    private fun command(
        settlementId: UUID,
        driverId: UUID,
        amount: BigDecimal,
        currency: String = "PLN",
    ) =
        PostDriverSettlementCommand(
            eventId = UUID.randomUUID(),
            eventType = "driver.settlement.completed",
            settlementId = settlementId,
            driverId = driverId,
            currency = currency,
            amount = amount,
            driverFullName = "DRIVER:$driverId",
            occurredAt = Instant.parse("2026-08-23T10:00:00Z"),
        )

    private fun assertAmount(
        expected: BigDecimal,
        actual: BigDecimal,
    ) {
        assertEquals(
            0,
            expected.compareTo(actual),
            "Expected amount $expected but was $actual",
        )
    }
}
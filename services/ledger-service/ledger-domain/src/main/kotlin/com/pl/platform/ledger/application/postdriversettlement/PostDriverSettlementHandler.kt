package com.pl.platform.ledger.application.postdriversettlement

import com.pl.platform.ledger.application.port.LedgerAccountRepository
import com.pl.platform.ledger.application.port.LedgerBalances
import com.pl.platform.ledger.application.port.LedgerTransactionRepository
import com.pl.platform.ledger.application.port.ProcessedEvents
import com.pl.platform.ledger.domain.LedgerAccount
import com.pl.platform.ledger.domain.LedgerAccountOwnerType
import com.pl.platform.ledger.domain.LedgerTransaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Posts a completed driver settlement as a balanced transaction between the platform account and the driver
 * account (created on first settlement). Idempotent twice: per event (inbox) and per settlement (reference).
 */
@Service
class PostDriverSettlementHandler(
    private val ledgerAccountRepository: LedgerAccountRepository,
    private val ledgerTransactionRepository: LedgerTransactionRepository,
    private val processedEvents: ProcessedEvents,
    private val balances: LedgerBalances,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun handle(command: PostDriverSettlementCommand) {
        if (processedEvents.isProcessed(command.eventId)) {
            log.info("Event {} already processed, skipping", command.eventId)
            return
        }

        if (!ledgerTransactionRepository.existsByReference(SETTLEMENT, command.settlementId)) {
            post(command)
        }

        processedEvents.markProcessed(command.eventId, command.eventType)
    }

    private fun post(command: PostDriverSettlementCommand) {
        // The platform accounts are seeded by migration V4; a missing one is a deployment error, not bad input.
        val platformAccount =
            checkNotNull(
                ledgerAccountRepository.findByOwnerAndCurrency(LedgerAccountOwnerType.PLATFORM, null, command.currency),
            ) { "Platform ledger account not found for currency ${command.currency}" }

        val driverAccount =
            ledgerAccountRepository.findByOwnerAndCurrency(LedgerAccountOwnerType.DRIVER, command.driverId, command.currency)
                ?: ledgerAccountRepository.save(
                    LedgerAccount.driver(
                        id = UUID.randomUUID(),
                        name = command.driverFullName,
                        driverId = command.driverId,
                        currency = command.currency,
                    ),
                )

        val transaction =
            LedgerTransaction.driverSettlement(
                id = UUID.randomUUID(),
                settlementId = command.settlementId,
                currency = command.currency,
                amount = command.amount,
                platformAccountId = platformAccount.id,
                driverAccountId = driverAccount.id,
                occurredAt = command.occurredAt,
            )
        ledgerTransactionRepository.save(transaction)
        balances.apply(transaction)
    }

    private companion object {
        const val SETTLEMENT = "SETTLEMENT"
    }
}

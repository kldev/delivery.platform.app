package com.pl.platform.settlement.infrastructure.seed

import com.pl.platform.settlement.application.port.SettlementRepository
import com.pl.platform.settlement.domain.Settlement
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Component
@Profile("dev")
class SettlementSeeder(
    private val settlementRepository: SettlementRepository,
) : CommandLineRunner {

    override fun run(vararg args: String) {
        seed()
    }

    private fun seed() {

        if (settlementRepository.exists()) return

        logger.info("Seeding settlements")

        val settlements = listOf(
            createSettlement(
                deliveryAmount = "100.00",
                currency = "EUR",
                basePercentage = "70.00",
                nightPercentage = "0.00",
                weekendPercentage = "0.00",
                distancePercentage = "0.00",
                totalPercentage = "70.00",
                driverAmount = "70.00",
                distanceKm = "25.00",
                hoursAgo = 48,
            ),
            createSettlement(
                deliveryAmount = "150.00",
                currency = "EUR",
                basePercentage = "70.00",
                nightPercentage = "5.00",
                weekendPercentage = "0.00",
                distancePercentage = "0.00",
                totalPercentage = "75.00",
                driverAmount = "112.50",
                distanceKm = "50.00",
                hoursAgo = 24,
            ),
            createSettlement(
                deliveryAmount = "200.00",
                currency = "EUR",
                basePercentage = "70.00",
                nightPercentage = "0.00",
                weekendPercentage = "5.00",
                distancePercentage = "10.00",
                totalPercentage = "85.00",
                driverAmount = "170.00",
                distanceKm = "150.00",
                hoursAgo = 12,
            ),
            createSettlement(
                deliveryAmount = "200.00",
                currency = "PLN",
                basePercentage = "70.00",
                nightPercentage = "0.00",
                weekendPercentage = "5.00",
                distancePercentage = "10.00",
                totalPercentage = "85.00",
                driverAmount = "170.00",
                distanceKm = "150.00",
                hoursAgo = 12,
            ),
            createSettlement(
                deliveryAmount = "200.00",
                currency = "PLN",
                basePercentage = "75.00",
                nightPercentage = "0.00",
                weekendPercentage = "5.00",
                distancePercentage = "10.00",
                totalPercentage = "85.00",
                driverAmount = "170.00",
                distanceKm = "150.00",
                hoursAgo = 12,
            ),
        )

        settlements.forEach(settlementRepository::create)

        logger.info("Seeding settlements completed")
    }

    private fun createSettlement(
        deliveryAmount: String,
        currency: String,
        basePercentage: String,
        nightPercentage: String,
        weekendPercentage: String,
        distancePercentage: String,
        totalPercentage: String,
        driverAmount: String,
        distanceKm: String,
        hoursAgo: Long,
    ): Settlement =
        Settlement.create(
            deliveryId = UUID.randomUUID(),
            driverId = UUID.randomUUID(),
            driverFullName = "John Connor",
            deliveryAmount = BigDecimal(deliveryAmount),
            currency = currency,
            basePercentage = BigDecimal(basePercentage),
            nightPercentage = BigDecimal(nightPercentage),
            weekendPercentage = BigDecimal(weekendPercentage),
            distancePercentage = BigDecimal(distancePercentage),
            totalPercentage = BigDecimal(totalPercentage),
            driverAmount = BigDecimal(driverAmount),
            distanceKm = BigDecimal(distanceKm),
            completedAt = Instant.now().minus(hoursAgo, ChronoUnit.HOURS),
            now = Instant.now(),
        )

        companion object{
            private val logger = LoggerFactory.getLogger(SettlementSeeder::class.java)
        }

}
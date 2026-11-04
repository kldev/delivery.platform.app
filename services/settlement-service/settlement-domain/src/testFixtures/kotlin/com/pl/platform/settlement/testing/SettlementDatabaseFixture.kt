package com.pl.platform.settlement.testing

import com.pl.platform.settlement.infrastructure.persistence.SettlementJpaEntity
import com.pl.platform.settlement.infrastructure.persistence.SpringDataSettlementRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Component
class SettlementDatabaseFixture(
    private val settlementRepository: SpringDataSettlementRepository,
) {

    fun create(
        id: UUID = UUID.randomUUID(),
        deliveryId: UUID = UUID.randomUUID(),
        driverId: UUID = UUID.randomUUID(),
        driverFullName: String = "John Connor",
        deliveryAmount: BigDecimal = BigDecimal("100.00"),
        currency: String = "EUR",
        basePercentage: BigDecimal = BigDecimal("70.00"),
        nightPercentage: BigDecimal = BigDecimal.ZERO,
        weekendPercentage: BigDecimal = BigDecimal.ZERO,
        distancePercentage: BigDecimal = BigDecimal.ZERO,
        totalPercentage: BigDecimal = BigDecimal("70.00"),
        driverAmount: BigDecimal = BigDecimal("70.00"),
        distanceKm: BigDecimal = BigDecimal("50.00"),
        completedAt: Instant = Instant.now(),
    ): UUID {

        val settlement = SettlementTestFactory.create(
            id = id,
            deliveryId = deliveryId,
            driverId = driverId,
            driverFullName = driverFullName,
            deliveryAmount = deliveryAmount,
            currency = currency,
            basePercentage = basePercentage,
            nightPercentage = nightPercentage,
            weekendPercentage = weekendPercentage,
            distancePercentage = distancePercentage,
            totalPercentage = totalPercentage,
            driverAmount = driverAmount,
            distanceKm = distanceKm,
            completedAt = completedAt,
        )

        settlementRepository.saveAndFlush(
            SettlementJpaEntity.create(settlement)
        )

        return settlement.id.value
    }
}
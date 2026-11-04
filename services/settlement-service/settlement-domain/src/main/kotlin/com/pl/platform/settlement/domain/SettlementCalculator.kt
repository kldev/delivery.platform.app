package com.pl.platform.settlement.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

/**
 * The driver's share of a delivery: the BASE rate plus NIGHT (22:00-06:00), WEEKEND and LONG_DISTANCE
 * (> 100 km) bonuses, judged in Europe/Warsaw at the time the delivery was completed.
 */
object SettlementCalculator {
    private val ZONE: ZoneId = ZoneId.of("Europe/Warsaw")
    private val LONG_DISTANCE_KM = BigDecimal("100")
    private val HUNDRED = BigDecimal("100")

    fun calculate(
        deliveryId: UUID,
        driverId: UUID,
        driverFullName: String,
        deliveryAmount: BigDecimal,
        currency: String,
        distanceKm: BigDecimal,
        completedAt: Instant,
        rates: List<SettlementRate>,
        now: Instant,
    ): Settlement {
        // Rates are seeded by migration V3; a missing BASE rate is a deployment error, not bad input.
        val base = checkNotNull(rates.rate("BASE")) { "Settlement rate 'BASE' is not configured" }
        val night = if (isNight(completedAt)) rates.rate("NIGHT") ?: BigDecimal.ZERO else BigDecimal.ZERO
        val weekend = if (isWeekend(completedAt)) rates.rate("WEEKEND") ?: BigDecimal.ZERO else BigDecimal.ZERO
        val distance = if (distanceKm > LONG_DISTANCE_KM) rates.rate("LONG_DISTANCE") ?: BigDecimal.ZERO else BigDecimal.ZERO
        val total = base + night + weekend + distance

        return Settlement.create(
            deliveryId = deliveryId,
            driverId = driverId,
            driverFullName = driverFullName,
            deliveryAmount = deliveryAmount,
            currency = currency,
            basePercentage = base,
            nightPercentage = night,
            weekendPercentage = weekend,
            distancePercentage = distance,
            totalPercentage = total,
            // Money: two decimals like the column, so the event carries exactly what is stored.
            driverAmount = deliveryAmount.multiply(total).divide(HUNDRED, 2, RoundingMode.HALF_UP),
            distanceKm = distanceKm,
            completedAt = completedAt,
            now = now,
        )
    }

    private fun List<SettlementRate>.rate(code: String): BigDecimal? = firstOrNull { it.code == code }?.percentage

    private fun isNight(at: Instant): Boolean {
        val time = at.atZone(ZONE).toLocalTime()
        return time >= LocalTime.of(22, 0) || time < LocalTime.of(6, 0)
    }

    private fun isWeekend(at: Instant): Boolean = at.atZone(ZONE).dayOfWeek in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
}

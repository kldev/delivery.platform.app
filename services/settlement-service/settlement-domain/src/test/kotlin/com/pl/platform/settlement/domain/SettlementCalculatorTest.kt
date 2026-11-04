package com.pl.platform.settlement.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class SettlementCalculatorTest {
    private val rates =
        listOf(rate("BASE", "70.00"), rate("NIGHT", "5.00"), rate("WEEKEND", "5.00"), rate("LONG_DISTANCE", "10.00"))

    // 2026-11-04 is a Wednesday; times below are UTC, Warsaw is UTC+1 in November.
    private val weekdayNoon = Instant.parse("2026-11-04T11:00:00Z")

    @Test
    fun `a weekday daytime short delivery gets the base rate`() {
        val settlement = calculate("100.00", "20", weekdayNoon)

        assertThat(settlement.totalPercentage).isEqualByComparingTo("70")
        assertThat(settlement.driverAmount).isEqualByComparingTo("70.00")
    }

    @Test
    fun `night, weekend and long distance add their bonuses`() {
        val saturdayNight = Instant.parse("2026-11-07T22:30:00Z") // 23:30 in Warsaw

        val settlement = calculate("200.00", "150", saturdayNight)

        assertThat(settlement.nightPercentage).isEqualByComparingTo("5")
        assertThat(settlement.weekendPercentage).isEqualByComparingTo("5")
        assertThat(settlement.distancePercentage).isEqualByComparingTo("10")
        assertThat(settlement.totalPercentage).isEqualByComparingTo("90")
        assertThat(settlement.driverAmount).isEqualByComparingTo("180.00")
    }

    @Test
    fun `night is judged in Warsaw time`() {
        val warsawFiveAm = Instant.parse("2026-11-04T04:00:00Z")
        val warsawSixAm = Instant.parse("2026-11-04T05:00:00Z")

        assertThat(calculate("100.00", "20", warsawFiveAm).nightPercentage).isEqualByComparingTo("5")
        assertThat(calculate("100.00", "20", warsawSixAm).nightPercentage).isEqualByComparingTo("0")
    }

    @Test
    fun `the driver amount is rounded to cents like the stored value`() {
        val settlement = calculate("99.99", "20", Instant.parse("2026-11-04T22:30:00Z")) // 75%

        assertThat(settlement.driverAmount).isEqualTo(BigDecimal("74.99"))
    }

    @Test
    fun `a missing base rate is a configuration error`() {
        assertThatThrownBy {
            SettlementCalculator.calculate(
                UUID.randomUUID(), UUID.randomUUID(), "Ada", BigDecimal("10"), "PLN", BigDecimal("1"), weekdayNoon,
                rates.filter { it.code != "BASE" }, weekdayNoon,
            )
        }.isInstanceOf(IllegalStateException::class.java).hasMessageContaining("BASE")
    }

    private fun calculate(
        amount: String,
        km: String,
        completedAt: Instant,
    ) = SettlementCalculator.calculate(
        deliveryId = UUID.randomUUID(),
        driverId = UUID.randomUUID(),
        driverFullName = "Ada Lovelace",
        deliveryAmount = BigDecimal(amount),
        currency = "PLN",
        distanceKm = BigDecimal(km),
        completedAt = completedAt,
        rates = rates,
        now = completedAt,
    )

    private fun rate(
        code: String,
        percentage: String,
    ) = SettlementRate(code, code, BigDecimal(percentage), true, Instant.EPOCH)
}

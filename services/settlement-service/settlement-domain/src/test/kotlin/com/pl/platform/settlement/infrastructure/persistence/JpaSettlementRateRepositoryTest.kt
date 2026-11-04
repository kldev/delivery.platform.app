package com.pl.platform.settlement.infrastructure.persistence

import com.pl.platform.settlement.testing.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

class JpaSettlementRateRepositoryTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var repository: SpringDataSettlementRateRepository

    @Test
    fun `should find active settlement rates`() {

        val rates = repository.findAllByActiveTrue()

        assertThat(rates)
            .isNotEmpty

        assertThat(rates)
            .extracting<String> { it.code }
            .contains(
                "BASE",
                "NIGHT",
                "WEEKEND",
                "LONG_DISTANCE",
            )
    }

    @Test
    fun `should load base settlement rate from flyway seed`() {

        val rate =
            repository.findById("BASE")
                .orElseThrow()

        assertThat(rate.code)
            .isEqualTo("BASE")

        assertThat(rate.name)
            .isEqualTo("Base driver rate")

        assertThat(rate.percentage)
            .isEqualByComparingTo(BigDecimal("70.00"))

        assertThat(rate.active)
            .isTrue()

        assertThat(rate.createdAt)
            .isNotNull
    }

    @Test
    fun `should load night settlement rate from flyway seed`() {

        val rate =
            repository.findById("NIGHT")
                .orElseThrow()

        assertThat(rate.code)
            .isEqualTo("NIGHT")

        assertThat(rate.percentage)
            .isEqualByComparingTo(BigDecimal("5.00"))

        assertThat(rate.active)
            .isTrue()
    }

    @Test
    fun `should load weekend settlement rate from flyway seed`() {

        val rate =
            repository.findById("WEEKEND")
                .orElseThrow()

        assertThat(rate.percentage)
            .isEqualByComparingTo(BigDecimal("5.00"))

        assertThat(rate.active)
            .isTrue()
    }

    @Test
    fun `should load long distance settlement rate from flyway seed`() {

        val rate =
            repository.findById("LONG_DISTANCE")
                .orElseThrow()

        assertThat(rate.percentage)
            .isEqualByComparingTo(BigDecimal("10.00"))

        assertThat(rate.active)
            .isTrue()
    }
}
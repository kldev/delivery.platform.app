package com.pl.platform.ledger.migration

import com.pl.platform.ledger.testing.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.util.UUID
import javax.sql.DataSource

/** V9 (flip settlement entries) and V10 (rebuild balances) on data posted the old way, in a schema of its own. */
class SettlementDirectionMigrationTest : BaseIntegrationTest() {
    @Autowired
    lateinit var dataSource: DataSource

    @Test
    fun `settlements posted before V9 end up as platform debit, driver credit, with matching balances`() {
        val schema = "mig_" + UUID.randomUUID().toString().replace("-", "").take(12)
        fun flyway(target: String) =
            Flyway
                .configure()
                .dataSource(dataSource)
                .schemas(schema)
                .locations("classpath:db/migration")
                .target(target)
                .load()

        flyway("8").migrate()

        val platform = UUID.randomUUID()
        val driver = UUID.randomUUID()
        val transaction = UUID.randomUUID()
        jdbcTemplate.execute(
            """
            INSERT INTO $schema.ledger_accounts (id, owner_type, owner_id, currency, created_at, name) VALUES
                ('$platform', 'PLATFORM', NULL, 'USD', now(), 'Platform USD'),
                ('$driver', 'DRIVER', '${UUID.randomUUID()}', 'USD', now(), 'Driver');
            INSERT INTO $schema.ledger_transactions (id, type, reference_type, reference_id, currency, occurred_at, created_at)
                VALUES ('$transaction', 'DRIVER_SETTLEMENT', 'SETTLEMENT', '${UUID.randomUUID()}', 'USD', now(), now());
            INSERT INTO $schema.ledger_entries (id, transaction_id, account_id, amount, type, created_at) VALUES
                ('${UUID.randomUUID()}', '$transaction', '$platform', 40, 'CREDIT', now()),
                ('${UUID.randomUUID()}', '$transaction', '$driver', 40, 'DEBIT', now());
            """.trimIndent(),
        )

        flyway("latest").migrate()

        fun typeOf(account: UUID) =
            jdbcTemplate.queryForObject("SELECT type FROM $schema.ledger_entries WHERE account_id = ?", String::class.java, account)

        fun balanceOf(account: UUID) =
            jdbcTemplate.queryForObject(
                "SELECT balance FROM $schema.ledger_account_balances WHERE account_id = ?",
                BigDecimal::class.java,
                account,
            )

        assertThat(typeOf(platform)).isEqualTo("DEBIT")
        assertThat(typeOf(driver)).isEqualTo("CREDIT")
        assertThat(balanceOf(driver)).isEqualByComparingTo("40")
        assertThat(balanceOf(platform)).isEqualByComparingTo("-40")
        jdbcTemplate.execute("DROP SCHEMA $schema CASCADE")
    }
}

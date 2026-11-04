package com.pl.platform.notification.migration

import com.pl.platform.notification.testing.BaseIntegrationTest
import io.agroal.api.AgroalDataSource
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import java.util.UUID

/** V4 on rows written the old way (plain address from the API, JSON from the sender), in a schema of its own. */
@QuarkusTest
class RecipientMigrationTest : BaseIntegrationTest() {
    @Inject
    lateinit var dataSource: AgroalDataSource

    @Test
    fun `old recipients become JSON objects and stuck PROCESSING rows get a claim time`() {
        val schema = "mig_" + UUID.randomUUID().toString().replace("-", "").take(12)
        fun flyway(target: String) =
            Flyway.configure().dataSource(dataSource).schemas(schema).locations("classpath:db/migration").target(target).load()

        flyway("3").migrate()
        val plain = UUID.randomUUID()
        val json = UUID.randomUUID()
        val empty = UUID.randomUUID()
        dataSource.connection.use { connection ->
            connection.createStatement().execute(
                """
                INSERT INTO $schema.notifications (id, event_id, event_type, recipient, channel, payload, status) VALUES
                    ('$plain', '${UUID.randomUUID()}', 'x', 'user@example.com', 'EMAIL', '{}', 'PENDING'),
                    ('$json', '${UUID.randomUUID()}', 'x', '{"fullName":"Ada","phoneNumber":"+48123","email":"ada@example.com"}', 'SMS', '{}', 'PROCESSING'),
                    ('$empty', '${UUID.randomUUID()}', 'x', '', 'PUSH', '{}', 'PENDING')
                """.trimIndent(),
            )
        }

        flyway("latest").migrate()

        dataSource.connection.use { connection ->
            fun query(sql: String): String? =
                connection.createStatement().executeQuery(sql).use { rs -> rs.next(); rs.getString(1) }

            assertThat(query("SELECT recipient->>'email' FROM $schema.notifications WHERE id = '$plain'")).isEqualTo("user@example.com")
            assertThat(query("SELECT recipient->>'phoneNumber' FROM $schema.notifications WHERE id = '$json'")).isEqualTo("+48123")
            assertThat(query("SELECT recipient::text FROM $schema.notifications WHERE id = '$empty'")).isNull()
            assertThat(query("SELECT claimed_at IS NOT NULL FROM $schema.notifications WHERE id = '$json'")).isEqualTo("t")
            connection.createStatement().execute("DROP SCHEMA $schema CASCADE")
        }
    }
}

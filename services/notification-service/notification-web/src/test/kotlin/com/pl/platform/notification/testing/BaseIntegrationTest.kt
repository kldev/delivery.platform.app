package com.pl.platform.notification.testing

import io.quarkus.test.common.QuarkusTestResource
import io.vertx.mutiny.sqlclient.Pool
import jakarta.inject.Inject

@QuarkusTestResource(PostgresTestResource::class)
abstract class BaseIntegrationTest {
    @Inject
    lateinit var pool: Pool

    protected fun cleanDatabase() {
        sql("TRUNCATE TABLE notifications, processed_events CASCADE")
    }

    protected fun sql(statement: String) {
        pool.query(statement).execute().await().indefinitely()
    }
}

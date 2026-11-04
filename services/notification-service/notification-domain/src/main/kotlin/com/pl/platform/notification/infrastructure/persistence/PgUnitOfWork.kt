package com.pl.platform.notification.infrastructure.persistence

import com.pl.platform.notification.application.port.Transaction
import com.pl.platform.notification.application.port.UnitOfWork
import io.smallrye.mutiny.Uni
import io.vertx.mutiny.sqlclient.Pool
import io.vertx.mutiny.sqlclient.SqlConnection
import jakarta.enterprise.context.ApplicationScoped

/** The connection of an open transaction; every statement in it must go through [connection], not the pool. */
class PgTransaction(
    val connection: SqlConnection,
) : Transaction

internal val Transaction.connection: SqlConnection
    get() = (this as PgTransaction).connection

@ApplicationScoped
class PgUnitOfWork(
    private val pool: Pool,
) : UnitOfWork {
    override fun <T> inTransaction(work: (Transaction) -> Uni<T>): Uni<T> =
        pool.withTransaction { connection -> work(PgTransaction(connection)) }
}

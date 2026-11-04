package com.pl.platform.notification.application.port

import io.smallrye.mutiny.Uni

/** An open database transaction; repositories that take it run their statements inside it. */
interface Transaction

/** Runs [work] in one transaction: everything it does through [Transaction] commits or rolls back together. */
interface UnitOfWork {
    fun <T> inTransaction(work: (Transaction) -> Uni<T>): Uni<T>
}

package com.pl.platform.ledger.web.shared

import java.time.LocalDate

/** A request parameter that is valid alone but not together with others; 400 `validation` with [field]. */
class InvalidRequestException(
    val field: String,
    message: String,
) : RuntimeException(message)

fun requireDateRange(
    from: LocalDate?,
    to: LocalDate?,
) {
    if (from != null && to != null && from.isAfter(to)) {
        throw InvalidRequestException("from", "must not be after to")
    }
}

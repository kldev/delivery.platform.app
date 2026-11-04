package com.pl.platform.notification.web.shared

/** A request parameter that fails a check the annotations cannot express; 400 `validation` with [field]. */
class InvalidRequestException(
    val field: String,
    message: String,
) : RuntimeException(message)

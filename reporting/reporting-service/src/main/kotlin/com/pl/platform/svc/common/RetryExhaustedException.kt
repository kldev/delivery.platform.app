package com.pl.platform.svc.common


class RetryExhaustedException(
    val operation: String,
    val attempts: Int,
    cause: Throwable
) : RuntimeException(
    "Retry exhausted for operation='$operation' after $attempts attempts",
    cause
)
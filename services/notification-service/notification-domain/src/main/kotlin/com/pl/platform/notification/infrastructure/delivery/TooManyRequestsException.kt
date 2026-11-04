package com.pl.platform.notification.infrastructure.delivery

class TooManyRequestsException(
    val retryAfterSeconds: Long?,
) : RuntimeException("Remote API returned HTTP 429")

/** 404 from delivery-service: the delivery or driver does not exist. */
class RemoteNotFoundException : RuntimeException("Remote API returned HTTP 404")

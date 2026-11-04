package com.pl.platform.svc.common

class TooManyRequestsException(
    val retryAfterSeconds: Long?
) : RuntimeException("Remote API returned HTTP 429")
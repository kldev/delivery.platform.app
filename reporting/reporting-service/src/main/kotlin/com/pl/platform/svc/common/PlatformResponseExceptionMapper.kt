package com.pl.platform.svc.common

import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper

class PlatformResponseExceptionMapper :
    ResponseExceptionMapper<RuntimeException> {

    override fun toThrowable(response: Response): RuntimeException? {
        if (response.status != Response.Status.TOO_MANY_REQUESTS.statusCode) {
            return null
        }

        val retryAfter = response
            .headers
            .getFirst("Retry-After")
            ?.toString()
            ?.toLongOrNull()

        return TooManyRequestsException(
            retryAfterSeconds = retryAfter
        )
    }

    override fun getPriority(): Int = 500
}
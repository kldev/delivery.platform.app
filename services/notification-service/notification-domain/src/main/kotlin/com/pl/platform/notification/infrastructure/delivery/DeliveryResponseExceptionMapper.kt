package com.pl.platform.notification.infrastructure.delivery

import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper

/** 429 and 404 become exceptions the directory handles; other errors keep the client default. */
class DeliveryResponseExceptionMapper : ResponseExceptionMapper<RuntimeException> {
    override fun toThrowable(response: Response): RuntimeException? =
        when (response.status) {
            Response.Status.TOO_MANY_REQUESTS.statusCode ->
                TooManyRequestsException(response.headers.getFirst("Retry-After")?.toString()?.toLongOrNull())
            Response.Status.NOT_FOUND.statusCode -> RemoteNotFoundException()
            else -> null
        }

    override fun getPriority(): Int = 500
}

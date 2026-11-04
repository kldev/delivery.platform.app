package com.pl.platform.svc.common

import io.quarkus.logging.Log
import jakarta.ws.rs.client.ClientRequestContext
import jakarta.ws.rs.client.ClientResponseContext
import jakarta.ws.rs.client.ClientResponseFilter
import jakarta.ws.rs.ext.Provider

@Provider
class RestClientResponseLoggingFilter : ClientResponseFilter {

    override fun filter(
        requestContext: ClientRequestContext,
        responseContext: ClientResponseContext
    ) {
        Log.debugf(
            "REST CLIENT <- %s %s -> %d",
            requestContext.method,
            requestContext.uri,
            responseContext.status
        )
    }
}
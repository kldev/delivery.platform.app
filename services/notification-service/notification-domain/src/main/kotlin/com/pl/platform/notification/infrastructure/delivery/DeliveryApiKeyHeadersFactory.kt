package com.pl.platform.notification.infrastructure.delivery

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.MultivaluedHashMap
import jakarta.ws.rs.core.MultivaluedMap
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory

@ApplicationScoped
class DeliveryApiKeyHeadersFactory(
    @ConfigProperty(name = "delivery-client.api-key")
    private val apiKey: String,
) : ClientHeadersFactory {

    override fun update(
        incomingHeaders: MultivaluedMap<String, String>,
        clientOutgoingHeaders: MultivaluedMap<String, String>,
    ): MultivaluedMap<String, String> {
        val headers = MultivaluedHashMap<String, String>()

        if (apiKey.isNotBlank()) {
            headers.add("X-Api-Key", apiKey)
        }
        return headers
    }
}
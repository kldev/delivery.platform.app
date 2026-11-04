package com.pl.platform.notification.web

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation

@Path("/")
class RootResource {

    @Operation(hidden = true)
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello() = "notification-svc"
}
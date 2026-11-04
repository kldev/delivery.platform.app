package com.pl.platform.notification.web.notifications

import com.fasterxml.jackson.databind.ObjectMapper
import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.notification.application.create.CreateNotificationHandler
import com.pl.platform.notification.application.getnotification.GetNotificationHandler
import com.pl.platform.notification.application.getnotification.GetNotificationQuery
import com.pl.platform.notification.application.listnotifications.ListNotificationsHandler
import com.pl.platform.notification.application.listnotifications.ListNotificationsQuery
import com.pl.platform.notification.application.port.PageQuery
import com.pl.platform.notification.domain.NotificationId
import com.pl.platform.notification.web.shared.InvalidRequestException
import io.smallrye.mutiny.Uni
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import org.jboss.resteasy.reactive.RestResponse
import java.net.URI
import java.time.DateTimeException
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/** Errors are application/problem+json with a `code`: 400 `validation`, 404 `notification.not-found`, 409 `notification.duplicate`. */
@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class NotificationResource(
    private val createNotificationHandler: CreateNotificationHandler,
    private val getNotificationHandler: GetNotificationHandler,
    private val listNotificationsHandler: ListNotificationsHandler,
    private val objectMapper: ObjectMapper,
) {
    @POST
    @Operation(
        summary = "Queue a notification",
        description = "Queued as PENDING and sent by the scheduler. One notification per event and channel (409 otherwise).",
    )
    fun createNotification(
        @Valid request: CreateNotificationRequest,
    ): Uni<RestResponse<NotificationResponse>> =
        createNotificationHandler.handle(request.toCommand()).map { notification ->
            RestResponse.ResponseBuilder
                .created<NotificationResponse>(URI.create("/api/notifications/${notification.id.value}"))
                .entity(NotificationResponse.from(notification, objectMapper))
                .build()
        }

    @GET
    @Path("/{notificationId}")
    @Operation(summary = "Get a notification")
    fun getNotification(
        @RestPath notificationId: UUID,
    ): Uni<NotificationResponse> =
        getNotificationHandler
            .handle(GetNotificationQuery(NotificationId(notificationId)))
            .map { NotificationResponse.from(it, objectMapper) }

    @GET
    @Operation(
        summary = "List notifications",
        description = "Newest first. `from` and `to` are days (of createdAt) in `timezone`, both inclusive.",
    )
    fun listNotifications(
        @Parameter(description = "PENDING, PROCESSING, SENT or FAILED")
        @RestQuery status: String?,
        @RestQuery eventId: UUID?,
        @Parameter(description = "First day (yyyy-MM-dd)")
        @RestQuery from: LocalDate?,
        @Parameter(description = "Last day (yyyy-MM-dd)")
        @RestQuery to: LocalDate?,
        @Parameter(description = "IANA time zone of from/to")
        @RestQuery @DefaultValue("Europe/Warsaw") timezone: String,
        @Parameter(description = "Zero-based page number")
        @RestQuery @DefaultValue("0") @Min(0) page: Int,
        @Parameter(description = "Page size, 1 to $MAX_PAGE_SIZE")
        @RestQuery @DefaultValue("100") @Min(1) @Max(MAX_PAGE_SIZE) size: Int,
    ): Uni<SliceResponse<NotificationResponse>> {
        if (from != null && to != null && from.isAfter(to)) throw InvalidRequestException("from", "must not be after to")
        val query =
            ListNotificationsQuery(
                status = status?.let(::parseStatus),
                eventId = eventId,
                from = from,
                to = to,
                timezone = parseZone(timezone),
                page = PageQuery(page = page, size = size),
            )
        return listNotificationsHandler.handle(query).map { slice ->
            SliceResponse(slice.content.map { NotificationResponse.from(it, objectMapper) }, slice.hasNext)
        }
    }

    // Parsed here, not by JAX-RS: an unknown enum value or zone must be a 400 with the field, not a 404/500.
    private fun parseStatus(value: String) =
        NotificationStatusDto.entries.firstOrNull { it.name == value }?.toDomain()
            ?: throw InvalidRequestException("status", "must be one of ${NotificationStatusDto.entries.joinToString()}")

    private fun parseZone(value: String): ZoneId =
        try {
            ZoneId.of(value)
        } catch (_: DateTimeException) {
            throw InvalidRequestException("timezone", "unknown time zone")
        }

    private companion object {
        const val MAX_PAGE_SIZE = 500L
    }
}

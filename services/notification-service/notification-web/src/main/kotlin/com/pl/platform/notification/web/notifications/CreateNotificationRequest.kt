package com.pl.platform.notification.web.notifications

import com.fasterxml.jackson.databind.node.ObjectNode
import com.pl.platform.notification.application.create.CreateNotificationCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.util.UUID

// Required fields are nullable: a missing field then fails validation with its name.
data class CreateNotificationRequest(
    @field:NotNull
    val eventId: UUID?,
    @field:NotBlank
    @field:Size(max = 255)
    val eventType: String?,
    @field:NotNull
    val channel: NotificationChannelDto?,
    @field:Schema(description = "Any JSON object; a `deliveryId` in it is used to find the driver when recipient is omitted")
    @field:NotNull
    val payload: ObjectNode?, // ObjectNode, not JsonNode: JSON null must arrive as Kotlin null for @NotNull
    @field:Schema(description = "Omit to notify the driver of the delivery named in payload.deliveryId")
    @field:Valid
    val recipient: RecipientDto? = null,
) {
    // Called after validation passed, so the required fields are present.
    fun toCommand() =
        CreateNotificationCommand(
            eventId = eventId!!,
            eventType = eventType!!.trim(),
            channel = channel!!.toDomain(),
            payload = payload!!.toString(),
            recipient = recipient?.toDomain(),
        )
}

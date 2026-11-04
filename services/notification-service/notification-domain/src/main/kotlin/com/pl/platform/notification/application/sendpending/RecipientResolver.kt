package com.pl.platform.notification.application.sendpending

import com.fasterxml.jackson.databind.ObjectMapper
import com.pl.platform.notification.application.port.DriverDirectory
import com.pl.platform.notification.application.port.NotificationQueue
import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.domain.NotificationRecipient
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

/**
 * The recipient given at creation, or the driver of the delivery named in the payload (`deliveryId`). A resolved
 * driver is saved on the notification, so a retry does not ask delivery-service again.
 */
@ApplicationScoped
class RecipientResolver(
    private val drivers: DriverDirectory,
    private val queue: NotificationQueue,
    private val objectMapper: ObjectMapper,
) {
    fun resolve(notification: Notification): Uni<NotificationRecipient?> {
        notification.recipient?.let { return Uni.createFrom().item(it) }

        val deliveryId = deliveryIdOf(notification.payload) ?: return Uni.createFrom().nullItem()

        return drivers.driverOfDelivery(deliveryId).flatMap { driver ->
            if (driver == null) {
                Uni.createFrom().nullItem()
            } else {
                queue.saveRecipient(notification.id, driver).replaceWith(driver)
            }
        }
    }

    private fun deliveryIdOf(payload: String): UUID? =
        runCatching {
            objectMapper
                .readTree(payload)
                .get("deliveryId")
                ?.takeIf { !it.isNull }
                ?.asText()
                ?.let(UUID::fromString)
        }.getOrNull()
}

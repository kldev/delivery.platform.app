package com.pl.platform.notification.application.port

import com.pl.platform.notification.domain.NotificationRecipient
import io.smallrye.mutiny.Uni
import java.util.UUID

/** Contact data of the driver of a delivery (from delivery-service). */
interface DriverDirectory {
    /** Null when the delivery is unknown or has no driver yet. */
    fun driverOfDelivery(deliveryId: UUID): Uni<NotificationRecipient?>
}

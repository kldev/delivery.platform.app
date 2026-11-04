package com.pl.platform.notification.testing

import com.pl.platform.notification.application.port.DriverDirectory
import com.pl.platform.notification.domain.NotificationRecipient
import io.quarkus.test.Mock
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Replaces the delivery-service client in tests: drivers are registered per delivery id. */
@Mock
@ApplicationScoped
class FakeDriverDirectory : DriverDirectory {
    private val drivers = ConcurrentHashMap<UUID, NotificationRecipient>()

    fun register(
        deliveryId: UUID,
        driver: NotificationRecipient,
    ) {
        drivers[deliveryId] = driver
    }

    override fun driverOfDelivery(deliveryId: UUID): Uni<NotificationRecipient?> = Uni.createFrom().item(drivers[deliveryId])
}

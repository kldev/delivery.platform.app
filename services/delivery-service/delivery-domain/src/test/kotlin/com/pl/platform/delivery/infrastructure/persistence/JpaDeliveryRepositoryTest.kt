package com.pl.platform.delivery.infrastructure.persistence

import com.pl.platform.delivery.testing.BaseIntegrationTest
import com.pl.platform.delivery.domain.Delivery
import com.pl.platform.delivery.domain.DeliveryStatus
import com.pl.platform.delivery.testing.DriverDatabaseFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
class JpaDeliveryRepositoryTest : BaseIntegrationTest() {

     @Autowired
    private lateinit var deliveryRepository: JpaDeliveryRepository

    @Autowired
    private lateinit var driverDatabaseFixture: DriverDatabaseFixture

    @Autowired
    private lateinit var springDataRepository: SpringDataDeliveryRepository

    @BeforeEach
    fun cleanUp() {
        springDataRepository.deleteAll()
    }

    @Test
    fun `should create and load delivery`() {

        val delivery = Delivery.create(
            pickupAddress = "Opole",
            deliveryAddress = "Wrocław",
            price = BigDecimal("200.00"),
            distanceKm = BigDecimal("50")
        )

        deliveryRepository.create(delivery)

        val loaded =
            deliveryRepository.findById(delivery.id)

        assertNotNull(loaded)
        assertEquals(delivery.id, loaded.id)
        assertEquals(delivery.driverId, loaded.driverId)
        assertEquals(
            delivery.pickupAddress,
            loaded.pickupAddress
        )
        assertEquals(
            delivery.deliveryAddress,
            loaded.deliveryAddress
        )
        assertEquals(
            DeliveryStatus.CREATED,
            loaded.status
        )
    }

    @Test
    fun `should update delivery status`() {
        val delivery = Delivery.create(
            pickupAddress = "Opole",
            deliveryAddress = "Wrocław",
            price = BigDecimal("200.00"),
            distanceKm = BigDecimal("50")
        )

        deliveryRepository.create(delivery)

        val driverId = driverDatabaseFixture.create()

        delivery.confirm()
        delivery.assign(driverId)
        deliveryRepository.update(delivery)

        val loaded =
            deliveryRepository.findById(delivery.id)

        assertNotNull(loaded)
        assertEquals(
            DeliveryStatus.ASSIGNED,
            loaded.status
        )
    }
}
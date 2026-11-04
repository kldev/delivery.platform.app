package com.pl.platform.delivery.infrastructure.query

import com.pl.platform.delivery.testing.BaseIntegrationTest
import com.pl.platform.delivery.application.liststatusdurations.ListStatusDurationsQuery
import com.pl.platform.delivery.application.port.PageQuery
import com.pl.platform.delivery.domain.DeliveryStatus
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import com.pl.platform.delivery.testing.DeliveryDatabaseFixture
import com.pl.platform.delivery.testing.DriverDatabaseFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import kotlin.test.Test

class JpaDeliveryStatusDurationQueryRepositoryTest : BaseIntegrationTest() {

    @Autowired
    lateinit var repository: JpaDeliveryStatusDurationQueryRepository

    @Autowired
    lateinit var deliveryDatabaseFixture: DeliveryDatabaseFixture

    @Autowired
    lateinit var driverDatabaseFixture: DriverDatabaseFixture

    @BeforeEach
    fun cleanUp() {
        cleanDatabase()
        seedTest()
    }

    fun seedTest() {
        deliveryDatabaseFixture.create()
        deliveryDatabaseFixture.create()

        val assigned = deliveryDatabaseFixture.create()
        val driver = driverDatabaseFixture.create()

        deliveryDatabaseFixture.changeStatus(
            assigned,
            DeliveryStatusJpa.ASSIGNED,
            driverId = driver
        )
    }

    @Test
    fun shouldReturnDeliveryStatusDuration() {

        val query = ListStatusDurationsQuery(
            deliveryId = null
        )

        val result = repository.search(query)

        assertThat(result).isNotNull()
        assertThat(result.content).isNotEmpty()
    }

    @Test
    fun shouldReturnDeliveryStatusDurationForSelectedDelivery() {

        val deliveryId = deliveryDatabaseFixture.create()

        val query = ListStatusDurationsQuery(
            deliveryId = deliveryId
        )

        val result = repository.search(query)

        assertThat(result).isNotNull()
        assertThat(result.content).isNotEmpty()

        assertThat(result.content)
            .allMatch { it.deliveryId == deliveryId }
    }

    @Test
    fun shouldReturnStatusHistoryForAssignedDelivery() {

        val deliveryId = deliveryDatabaseFixture.create()
        val driver = driverDatabaseFixture.create()

        deliveryDatabaseFixture.changeStatus(
            deliveryId,
            DeliveryStatusJpa.ASSIGNED,
            driverId = driver
        )

        val query = ListStatusDurationsQuery(
            deliveryId = deliveryId
        )

        val result = repository.search(query)

        assertThat(result.content).hasSize(2)

        assertThat(result.content.map { it.status })
            .containsExactly(
                DeliveryStatus.CREATED,
                DeliveryStatus.ASSIGNED
            )
    }

    @Test
    fun shouldReturnOnlySelectedDeliveryStatusHistory() {

        val firstDeliveryId = deliveryDatabaseFixture.create()
        val secondDeliveryId = deliveryDatabaseFixture.create()

        val driver = driverDatabaseFixture.create()

        deliveryDatabaseFixture.changeStatus(
            firstDeliveryId,
            DeliveryStatusJpa.ASSIGNED,
            driverId = driver
        )

        val query = ListStatusDurationsQuery(
            deliveryId = firstDeliveryId
        )

        val result = repository.search(query)

        assertThat(result.content)
            .allMatch { it.deliveryId == firstDeliveryId }

        assertThat(result.content)
            .noneMatch { it.deliveryId == secondDeliveryId }
    }

    @Test
    fun shouldReturnStatusDurationWithOpenLastStatus() {

        val deliveryId = deliveryDatabaseFixture.create()

        Thread.sleep(200)
        deliveryDatabaseFixture.changeStatus(deliveryId, DeliveryStatusJpa.CONFIRMED)

        val query = ListStatusDurationsQuery(
            deliveryId = deliveryId,
            page = PageQuery(page = 0, size = 20),
        )

        val result = repository.search(query)

        val currentStatus = result.content.last()

        // The current status is still open: no next status, so no end; the view counts its duration up to NOW().
        assertThat(currentStatus.statusTo)
            .isNull()

        assertThat(currentStatus.durationSeconds)
            .isGreaterThanOrEqualTo(BigDecimal.ZERO)
    }

}
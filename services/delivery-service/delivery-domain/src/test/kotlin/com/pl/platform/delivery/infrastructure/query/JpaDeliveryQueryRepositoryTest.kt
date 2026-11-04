package com.pl.platform.delivery.infrastructure.query

import com.pl.platform.delivery.testing.BaseIntegrationTest
import com.pl.platform.delivery.application.listdeliveries.ListDeliveriesQuery
import com.pl.platform.delivery.domain.DeliveryStatus
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import com.pl.platform.delivery.testing.DeliveryDatabaseFixture
import com.pl.platform.delivery.testing.DriverDatabaseFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

class JpaDeliveryQueryRepositoryTest : BaseIntegrationTest() {

    @Autowired
    lateinit var repository: JpaDeliveryQueryRepository

    @Autowired
    lateinit var deliveryDatabaseFixture: DeliveryDatabaseFixture

    @Autowired
    lateinit var driverDatabaseFixture: DriverDatabaseFixture

    @BeforeEach
    fun cleanUp() {
        cleanDatabase()
        seedTest()
    }

    fun seedTest(){
        deliveryDatabaseFixture.create()
        deliveryDatabaseFixture.create()
        val assigned = deliveryDatabaseFixture.create()
        val driver = driverDatabaseFixture.create()

        deliveryDatabaseFixture.changeStatus(assigned,
            DeliveryStatusJpa.ASSIGNED, driverId = driver)
    }

    @Test
    fun shouldReturnDeliveries() {

        val query = ListDeliveriesQuery()

        val result = repository.search(query)

        assertThat(result).isNotNull()
        assertThat(result.content).hasSize(3)
    }

    @Test
    fun shouldReturnDeliveriesWithStatusAssigned() {

        val query = ListDeliveriesQuery(status = DeliveryStatus.ASSIGNED, deliveryId = null)

        val result = repository.search(query)

        assertThat(result).isNotNull()
        assertThat(result.content).hasSize(1)
    }

    @Test
    fun shouldReturnSelectedDelivery() {

        val deliveryId = deliveryDatabaseFixture.create()
        val query = ListDeliveriesQuery(status = null, deliveryId = deliveryId)

        val result = repository.search(query)

        assertThat(result).isNotNull()
        assertThat(result.content).hasSize(1)
    }
}


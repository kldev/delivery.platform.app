package com.pl.platform.delivery.infrastructure.messaging.outbox

import com.pl.platform.common.messaging.OutboxStatus
import com.pl.platform.common.messaging.event.delivery.DeliveryCreatedEvent
import com.pl.platform.common.messaging.port.OutboxRepository
import com.pl.platform.delivery.testing.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import java.math.BigDecimal
import java.util.UUID

class OutboxPersistenceTest : BaseIntegrationTest() {

    @Autowired
    lateinit var outboxRepository: OutboxRepository

    @Autowired
    lateinit var springDataOutboxRepository: SpringDataOutboxRepository

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    @Test
    fun `should persist outbox message`() {

        val event =
            DeliveryCreatedEvent(
                deliveryId = UUID.randomUUID(),
                price = BigDecimal("200.99")
            )

        // Save and read in one transaction: once committed, the scheduled OutboxPublisher may publish it
        // before the assertions run and the status is no longer PENDING.
        val entity =
            transactionTemplate.execute {
                outboxRepository.save(event)
                springDataOutboxRepository.findByEventId(event.eventId)
            } ?: throw IllegalArgumentException("Event with id ${event.eventId} not found")

        assertThat(entity.module)
            .isEqualTo("delivery")

        assertThat(entity.aggregateId)
            .isEqualTo(event.deliveryId)

        assertThat(entity.eventType)
            .isEqualTo("delivery.created")


        assertThat(entity.payload)
            .contains(event.deliveryId.toString())

        assertThat(entity.status)
            .isEqualTo(OutboxStatus.PENDING)

        assertThat(entity.attempts)
            .isZero()
    }
}
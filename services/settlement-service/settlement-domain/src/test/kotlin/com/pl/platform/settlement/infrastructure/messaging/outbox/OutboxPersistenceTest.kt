package com.pl.platform.settlement.infrastructure.messaging.outbox

import com.pl.platform.common.messaging.OutboxStatus
import com.pl.platform.common.messaging.event.TestEvent
import com.pl.platform.common.messaging.port.OutboxRepository
import com.pl.platform.settlement.testing.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

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
            TestEvent(
                eventId = UUID.randomUUID(),
                aggregateId = UUID.randomUUID(),
                module = "test"
            )

        // Save and read in one transaction: once committed, the scheduled OutboxPublisher may publish it
        // before the assertions run and the status is no longer PENDING.
        val entity =
            transactionTemplate.execute {
                outboxRepository.save(event)
                springDataOutboxRepository.findByEventId(event.eventId)
            } ?: throw IllegalArgumentException("Event with id ${event.eventId} not found")

        assertThat(entity.module)
            .isEqualTo("test")

        assertThat(entity.aggregateId)
            .isEqualTo(event.aggregateId)

        assertThat(entity.eventType)
            .isEqualTo(event.eventType)


        assertThat(entity.payload)
            .contains(event.eventId.toString())

        assertThat(entity.status)
            .isEqualTo(OutboxStatus.PENDING)

        assertThat(entity.attempts)
            .isZero()
    }
}
package com.pl.platform.settlement.infrastructure.messaging

import com.pl.platform.settlement.domain.DomainException
import org.apache.kafka.common.TopicPartition
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff
import tools.jackson.core.JacksonException
import java.time.Duration

/**
 * A record that fails is retried (a database hiccup, or two events for one delivery racing on its unique
 * row), then published to the dead letter topic with the kafka_dlt-* headers, and the listener moves on. An
 * unreadable record or a domain error is not retried. Spring Boot
 * applies this CommonErrorHandler bean to the auto-configured listener container factory.
 */
@Configuration
@ConditionalOnProperty(prefix = "settlement", name = ["event-bus"], havingValue = "kafka")
class KafkaErrorHandlingConfiguration {
    @Bean
    fun kafkaErrorHandler(
        template: KafkaOperations<Any, Any>,
        @Value("\${settlement.kafka.dead-letter-topic}") deadLetterTopic: String,
        @Value("\${settlement.kafka.retries}") retries: Long,
        @Value("\${settlement.kafka.retry-backoff}") retryBackoff: Duration,
    ): DefaultErrorHandler {
        // Partition -1: let Kafka pick one, the DLQ does not mirror the partitions of the source topics.
        val recoverer = DeadLetterPublishingRecoverer(template) { _, _ -> TopicPartition(deadLetterTopic, -1) }
        return DefaultErrorHandler(recoverer, FixedBackOff(retryBackoff.toMillis(), retries)).apply {
            addNotRetryableExceptions(JacksonException::class.java, DomainException::class.java)
        }
    }
}

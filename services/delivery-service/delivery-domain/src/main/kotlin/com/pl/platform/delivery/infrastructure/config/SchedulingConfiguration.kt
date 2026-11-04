package com.pl.platform.delivery.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/** Outbox publisher and idempotency cleaner run on @Scheduled; enabled here so every host of the domain gets them. */
@Configuration
@EnableScheduling
class SchedulingConfiguration

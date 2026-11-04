package com.pl.platform.settlement.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/** The outbox publisher runs on @Scheduled; enabled here so every host of the domain gets it. */
@Configuration
@EnableScheduling
class SchedulingConfiguration

package com.pl.platform.reconciliation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

/** reconciliation-domain has no application class; @SpringBootTest in this project starts this one. */
@SpringBootApplication
@ConfigurationPropertiesScan
class ReconciliationDomainTestApplication

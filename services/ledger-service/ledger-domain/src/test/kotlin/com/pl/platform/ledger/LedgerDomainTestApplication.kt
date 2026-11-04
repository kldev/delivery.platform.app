package com.pl.platform.ledger

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

/** ledger-domain has no application class; @SpringBootTest in this project starts this one. */
@SpringBootApplication
@ConfigurationPropertiesScan
class LedgerDomainTestApplication

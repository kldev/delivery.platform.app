package com.pl.platform.ledger

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

// Base package com.pl.platform.ledger is the parent of both ledger-domain and ledger-web packages,
// so component, entity and repository scanning cover the domain jar without extra annotations.
@SpringBootApplication
@ConfigurationPropertiesScan
class LedgerServiceApplication

fun main(args: Array<String>) {
    runApplication<LedgerServiceApplication>(*args)
}

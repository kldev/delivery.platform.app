package com.pl.platform.reconciliation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

// Base package com.pl.platform.reconciliation is the parent of both reconciliation-domain and -web packages,
// so component, entity and repository scanning cover the domain jar without extra annotations.
@SpringBootApplication
@ConfigurationPropertiesScan
class ReconciliationServiceApplication

fun main(args: Array<String>) {
    runApplication<ReconciliationServiceApplication>(*args)
}

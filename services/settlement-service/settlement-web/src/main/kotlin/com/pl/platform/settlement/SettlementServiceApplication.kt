package com.pl.platform.settlement

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

// Base package com.pl.platform.settlement is the parent of both settlement-domain and settlement-web packages,
// so component, entity and repository scanning cover the domain jar without extra annotations.
@SpringBootApplication
@ConfigurationPropertiesScan
class SettlementServiceApplication

fun main(args: Array<String>) {
    runApplication<SettlementServiceApplication>(*args)
}

package com.pl.platform.delivery

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

// Base package com.pl.platform.delivery is the parent of both delivery-domain and delivery-web packages,
// so component, entity and repository scanning cover the domain jar without extra annotations.
@SpringBootApplication
@ConfigurationPropertiesScan
class DeliveryServiceApplication

fun main(args: Array<String>) {
    runApplication<DeliveryServiceApplication>(*args)
}

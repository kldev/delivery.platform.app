package com.pl.platform.delivery

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

/** delivery-domain has no application class; @SpringBootTest in this project starts this one. */
@SpringBootApplication
@ConfigurationPropertiesScan
class DeliveryDomainTestApplication

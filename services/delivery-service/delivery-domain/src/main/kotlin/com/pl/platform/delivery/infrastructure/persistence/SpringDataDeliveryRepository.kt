package com.pl.platform.delivery.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpringDataDeliveryRepository :
    JpaRepository<DeliveryJpaEntity, UUID>
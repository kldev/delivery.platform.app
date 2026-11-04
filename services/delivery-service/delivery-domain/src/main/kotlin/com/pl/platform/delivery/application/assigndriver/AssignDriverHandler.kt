package com.pl.platform.delivery.application.assigndriver

import com.pl.platform.common.messaging.event.delivery.DeliveryAssignedEvent
import com.pl.platform.common.messaging.port.OutboxRepository
import com.pl.platform.delivery.application.port.DeliveryRepository
import com.pl.platform.delivery.application.port.DeliveryState
import com.pl.platform.delivery.application.port.DriverRepository
import com.pl.platform.delivery.domain.NotFoundException
import com.pl.platform.delivery.domain.RuleViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AssignDriverHandler(
    private val deliveryRepository: DeliveryRepository,
    private val driverRepository: DriverRepository,
    private val outboxRepository: OutboxRepository,
) {
    @Transactional
    fun handle(command: AssignDriverCommand): DeliveryState {
        val delivery =
            deliveryRepository.findById(command.deliveryId)
                ?: throw NotFoundException("delivery", command.deliveryId.value)

        val driverId =
            if (command.driverId != null) {
                if (!driverRepository.existsById(command.driverId)) {
                    throw NotFoundException("driver", command.driverId.value)
                }
                command.driverId
            } else {
                driverRepository.findRandomId()
                    ?: throw RuleViolationException("driver.none-available", "There is no driver to assign")
            }

        delivery.assign(driverId.value)
        deliveryRepository.update(delivery)

        outboxRepository.save(
            DeliveryAssignedEvent(
                deliveryId = delivery.id.value,
                driverId = driverId.value,
            ),
        )

        return DeliveryState.from(delivery)
    }
}

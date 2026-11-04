package com.pl.platform.delivery.application.getdriver

import com.pl.platform.delivery.application.port.DriverDetails
import com.pl.platform.delivery.application.port.DriverRepository
import com.pl.platform.delivery.domain.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetDriverHandler(
    private val driverRepository: DriverRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: GetDriverQuery): DriverDetails {
        val driver =
            driverRepository.findById(query.driverId)
                ?: throw NotFoundException("driver", query.driverId.value)

        return DriverDetails.from(driver)
    }
}

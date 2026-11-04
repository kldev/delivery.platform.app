package com.pl.platform.delivery.application.listdrivers

import com.pl.platform.delivery.application.port.DriverDetails
import com.pl.platform.delivery.application.port.DriverRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListDriversHandler(
    private val driverRepository: DriverRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: ListDriversQuery): List<DriverDetails> =
        driverRepository.findAll(query.driverId).map(DriverDetails::from)
}

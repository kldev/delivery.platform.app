package com.pl.platform.svc.settlement

import com.pl.platform.svc.settlement.client.SettlementClient
import com.pl.platform.svc.settlement.client.model.SettlementResponse
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.util.*

@ApplicationScoped
class DriverSettlementService(
    @RestClient
    private val client: SettlementClient
) {

    fun getSettlements(driverId: UUID): List<SettlementResponse> {
        var page = 0
        var data = mutableListOf<SettlementResponse>()
        while (true) {
            val response = client.getSettlements(driverId = driverId, page = page, search = null, deliveryId = null);
            data.addAll(response.content)
            if (!response.hasNext)
                break;
            page++
        }
        return data;
    }
}
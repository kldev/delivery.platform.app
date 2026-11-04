package com.pl.platform.svc.settlement

import com.pl.platform.svc.common.SliceResponse
import com.pl.platform.svc.settlement.client.ReactiveSettlementClient
import com.pl.platform.svc.settlement.client.model.SettlementResponse
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.util.*

@ApplicationScoped
class ReactiveDriverSettlementService(
    @RestClient
    private val client: ReactiveSettlementClient
) {

    fun getSettlements(driverId: UUID): Uni<List<SettlementResponse>> =
        getSettlementsPage(driverId, 0)
            .onItem()
            .transform { pages ->
                pages.flatMap { it.content }
            }

    private fun getSettlementsPage(
        driverId: UUID,
        page: Int
    ): Uni<List<SliceResponse<SettlementResponse>>> =
        client.getSettlements(
            driverId = driverId,
            page = page,
            search = null,
            deliveryId = null
        ).onItem()
            .transformToUni { response ->
                if (response.hasNext) {
                    getSettlementsPage(driverId, page + 1)
                        .onItem()
                        .transform { nextPages ->
                            buildList {
                                add(response)
                                addAll(nextPages)
                            }
                        }
                } else {
                    Uni.createFrom().item(listOf(response))
                }
            }
}
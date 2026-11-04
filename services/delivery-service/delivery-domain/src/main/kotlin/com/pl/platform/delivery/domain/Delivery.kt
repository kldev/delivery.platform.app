package com.pl.platform.delivery.domain

import java.math.BigDecimal
import java.util.UUID

class Delivery private constructor(
    val id: DeliveryId,
    driverId: UUID?,
    val pickupAddress: String,
    val deliveryAddress: String,
    val distanceKm: BigDecimal,
    val price: BigDecimal,
    val currency: String,
    status: DeliveryStatus,
) {
    var driverId: UUID? = driverId
        private set

    var status: DeliveryStatus = status
        private set

    fun confirm() {
        requireStatus(DeliveryStatus.CREATED, "confirm")
        status = DeliveryStatus.CONFIRMED
    }

    fun assign(driverId: UUID) {
        requireStatus(DeliveryStatus.CONFIRMED, "assign a driver to")
        this.driverId = driverId
        status = DeliveryStatus.ASSIGNED
    }

    fun pickup() {
        requireStatus(DeliveryStatus.ASSIGNED, "pick up")
        status = DeliveryStatus.PICKED_UP
    }

    fun startTransit() {
        requireStatus(DeliveryStatus.PICKED_UP, "start")
        status = DeliveryStatus.IN_TRANSIT
    }

    fun deliver() {
        requireStatus(DeliveryStatus.IN_TRANSIT, "complete")
        status = DeliveryStatus.DELIVERED
    }

    fun cancel() {
        if (status !in CANCELLABLE) throw InvalidTransitionException(id, status, "cancel")
        status = DeliveryStatus.CANCELLED
    }

    /**
     * The payment service declined the payment: the delivery ends. Only a confirmed delivery has a payment, so only
     * a confirmed one can be declined. A dispatcher cannot cancel it then (see CANCELLABLE) - its payment is still
     * waiting for a decision, and accepting it later would assign a driver to a cancelled delivery.
     */
    fun declinePayment() {
        requireStatus(DeliveryStatus.CONFIRMED, "decline the payment of")
        status = DeliveryStatus.CANCELLED
    }

    private fun requireStatus(
        expected: DeliveryStatus,
        action: String,
    ) {
        if (status != expected) throw InvalidTransitionException(id, status, action)
    }

    companion object {
        private val CANCELLABLE =
            setOf(
                DeliveryStatus.CREATED,
                DeliveryStatus.ASSIGNED,
                DeliveryStatus.PICKED_UP,
                DeliveryStatus.IN_TRANSIT,
            )

        fun create(
            pickupAddress: String,
            deliveryAddress: String,
            price: BigDecimal,
            distanceKm: BigDecimal,
            currency: String = "PLN",
        ): Delivery {
            // Requests are validated before this point; a blank address here is a programming error.
            require(pickupAddress.isNotBlank()) {
                "Pickup address must not be blank"
            }

            require(deliveryAddress.isNotBlank()) {
                "Delivery address must not be blank"
            }

            return Delivery(
                id = DeliveryId.new(),
                driverId = null,
                pickupAddress = pickupAddress,
                deliveryAddress = deliveryAddress,
                status = DeliveryStatus.CREATED,
                price = price,
                distanceKm = distanceKm,
                currency = currency,
            )
        }

        fun reconstitute(
            id: DeliveryId,
            driverId: UUID?,
            pickupAddress: String,
            deliveryAddress: String,
            status: DeliveryStatus,
            price: BigDecimal,
            distanceKm: BigDecimal,
            currency: String?,
        ): Delivery =
            Delivery(
                id = id,
                driverId = driverId,
                pickupAddress = pickupAddress,
                deliveryAddress = deliveryAddress,
                status = status,
                price = price,
                distanceKm = distanceKm,
                currency = currency ?: "PLN",
            )
    }
}

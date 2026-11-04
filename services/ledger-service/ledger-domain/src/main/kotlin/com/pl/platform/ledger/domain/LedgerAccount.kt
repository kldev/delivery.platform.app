package com.pl.platform.ledger.domain

import java.util.UUID

class LedgerAccount private constructor(
    val id: UUID,
    val name: String,
    val ownerType: LedgerAccountOwnerType,
    val ownerId: UUID?,
    val currency: String,
) {

    init {
        require(name.isNotBlank()) {
            "Ledger account name must not be blank"
        }

        require(name.length <= 200) {
            "Ledger account name must not exceed 200 characters"
        }

        require(currency.matches(Regex("[A-Z]{3}"))) {
            "Currency must be a valid ISO 4217 code"
        }

        when (ownerType) {
            LedgerAccountOwnerType.PLATFORM ->
                require(ownerId == null) {
                    "Platform account cannot have ownerId"
                }

            LedgerAccountOwnerType.DRIVER ->
                require(ownerId != null) {
                    "Driver account must have ownerId"
                }
        }
    }

    companion object {

        fun platform(
            id: UUID,
            name: String,
            currency: String,
        ): LedgerAccount =
            LedgerAccount(
                id = id,
                name = name,
                ownerType = LedgerAccountOwnerType.PLATFORM,
                ownerId = null,
                currency = currency,
            )

        fun driver(
            id: UUID,
            name: String,
            driverId: UUID,
            currency: String,
        ): LedgerAccount =
            LedgerAccount(
                id = id,
                name = name,
                ownerType = LedgerAccountOwnerType.DRIVER,
                ownerId = driverId,
                currency = currency,
            )
    }
}
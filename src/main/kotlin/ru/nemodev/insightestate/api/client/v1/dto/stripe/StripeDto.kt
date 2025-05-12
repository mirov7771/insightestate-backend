package ru.nemodev.insightestate.api.client.v1.dto.stripe

import java.util.UUID


data class StripeRs (
    val clientSecret: String
)

data class StripeRq (
    val amount: Long,
    val currency: String = "usd",
    val userId: UUID
)

data class StripeSubscriptionRq (
    val userId: UUID,
    val tariffId: String
)

data class StripeRecurrentRq (
    val userId: UUID
)


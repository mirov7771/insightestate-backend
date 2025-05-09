package ru.nemodev.insightestate.api.client.v1.dto.stripe

import java.math.BigDecimal

data class StripeRs (
    val clientSecret: String
)

data class StripeRq (
    val amount: Long,
    val currency: String = "usd",
)

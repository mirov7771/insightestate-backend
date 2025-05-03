package ru.nemodev.insightestate.api.client.v1.dto.subscription

import ru.nemodev.insightestate.api.client.v1.dto.tariff.TariffRs
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class SubscriptionRq (
    val userId: UUID,
    val tariffId: UUID,
)

data class SubscriptionRs (
    val subscription: SubscriptionDto? = null,
    val tariffs: TariffRs? = null,
)

data class SubscriptionDto (
    val main: SubscriptionInfo? = null,
    val extra: SubscriptionInfo? = null,
)

data class SubscriptionInfo (
    val id: UUID? = null,
    val payDate: LocalDateTime? = null,
    val payAmount: BigDecimal? = null,
)

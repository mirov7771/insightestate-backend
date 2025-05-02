package ru.nemodev.insightestate.api.client.v1.dto.subscription

import java.util.*

data class SubscriptionRq (
    val userId: UUID,
    val tariffId: UUID,
)

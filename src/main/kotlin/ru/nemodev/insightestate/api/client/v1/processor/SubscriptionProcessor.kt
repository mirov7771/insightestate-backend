package ru.nemodev.insightestate.api.client.v1.processor

import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.converter.TariffConverter
import ru.nemodev.insightestate.api.client.v1.dto.subscription.SubscriptionRq
import ru.nemodev.insightestate.api.client.v1.dto.tariff.TariffRs
import ru.nemodev.insightestate.service.subscription.SubscriptionService
import java.util.*

interface SubscriptionProcessor {
    fun saveTariff(
        rq: SubscriptionRq
    )

    fun getTariff(
        userId: UUID
    ): TariffRs

    fun removeTariff(
        rq: SubscriptionRq
    )
}

@Component
class SubscriptionProcessorImpl (
    private val subscriptionService: SubscriptionService,
    private val converter: TariffConverter
) : SubscriptionProcessor {
    override fun saveTariff(
        rq: SubscriptionRq
    ) {
        subscriptionService.saveTariff(rq.userId, rq.tariffId)
    }

    override fun getTariff(userId: UUID): TariffRs {
        val tariffs = subscriptionService.getTariff(userId)
        if (tariffs.isEmpty())
            return TariffRs(null, null)
        return TariffRs(
            main = tariffs.filter { it.type == 0 }.map { converter.convert(it) },
            extra = tariffs.filter { it.type == 1 }.map { converter.convert(it) }
        )
    }

    override fun removeTariff(
        rq: SubscriptionRq
    ) {
        subscriptionService.removeTariff(rq.userId, rq.tariffId)
    }
}



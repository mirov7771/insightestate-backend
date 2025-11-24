package ru.nemodev.insightestate.api.client.v1.processor

import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.converter.TariffConverter
import ru.nemodev.insightestate.api.client.v1.dto.subscription.SubscriptionDto
import ru.nemodev.insightestate.api.client.v1.dto.subscription.SubscriptionInfo
import ru.nemodev.insightestate.api.client.v1.dto.subscription.SubscriptionRq
import ru.nemodev.insightestate.api.client.v1.dto.subscription.SubscriptionRs
import ru.nemodev.insightestate.api.client.v1.dto.tariff.TariffRs
import ru.nemodev.insightestate.service.subscription.SubscriptionService
import java.util.*

interface SubscriptionProcessor {
    fun saveTariff(
        rq: SubscriptionRq
    )

    fun getTariff(
        userId: UUID
    ): SubscriptionRs

    fun removeTariff(
        id: UUID
    )

    fun confirm(
        userId: UUID
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
        subscriptionService.saveTariff(rq.userId, rq.tariffId, rq.promoCode)
    }

    override fun getTariff(userId: UUID): SubscriptionRs {
        val tariffs = subscriptionService.getTariff(userId)
        if (tariffs.isEmpty())
            return SubscriptionRs()
        val subscription = subscriptionService.getSubscription(userId) ?: return SubscriptionRs()
        return SubscriptionRs(
            subscription = SubscriptionDto(
                main = SubscriptionInfo(
                    id = subscription.mainId,
                    payDate = subscription.mainPayDate,
                    payAmount = subscription.mainPayAmount,
                ),
                extra = SubscriptionInfo(
                    id = subscription.extraId,
                    payDate = subscription.extraPayDate,
                    payAmount = subscription.extraPayAmount,
                )
            ),
            tariffs = TariffRs(
                main = tariffs.filter { it.type == 0 }.map { converter.convert(it) },
                extra = tariffs.filter { it.type == 1 }.map { converter.convert(it) }
            ))
    }

    override fun removeTariff(
        id: UUID
    ) {
        subscriptionService.removeTariff(id)
    }

    override fun confirm(userId: UUID) {
        subscriptionService.confirm(userId)
    }
}



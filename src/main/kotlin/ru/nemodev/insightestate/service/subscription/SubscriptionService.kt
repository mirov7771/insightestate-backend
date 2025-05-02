package ru.nemodev.insightestate.service.subscription

import org.springframework.stereotype.Service
import ru.nemodev.insightestate.entity.SubscriptionEntity
import ru.nemodev.insightestate.entity.TariffEntity
import ru.nemodev.insightestate.repository.SubscriptionRepository
import ru.nemodev.insightestate.service.tariff.TariffService
import java.time.LocalDateTime
import java.util.*

interface SubscriptionService {
    fun saveTariff(
        userId: UUID,
        tariffId: UUID
    )

    fun getTariff(
        userId: UUID
    ): List<TariffEntity>

    fun removeTariff(
        userId: UUID,
        tariffId: UUID
    )
}

@Service
class SubscriptionServiceImpl (
    private val subscriptionRepository: SubscriptionRepository,
    private val tariffService: TariffService,
) : SubscriptionService {

    override fun saveTariff(
        userId: UUID,
        tariffId: UUID
    ) {
        val tariff = tariffService.findById(tariffId)
        val subscription = subscriptionRepository.findByUserId(userId)
        createOrUpdateTariff(userId, tariff, subscription)
    }

    override fun getTariff(
        userId: UUID
    ): List<TariffEntity> {
        val list = mutableListOf<TariffEntity>()
        val subscription = subscriptionRepository.findByUserId(userId) ?: return list.toList()
        if (subscription.mainId != null) {
            list.add(
                tariffService.findById(subscription.mainId!!)
            )
        }
        if (subscription.extraId != null) {
            list.add(
                tariffService.findById(subscription.extraId!!)
            )
        }
        return list.toList()
    }

    override fun removeTariff(
        userId: UUID,
        tariffId: UUID
    ) {
        val tariff = tariffService.findById(tariffId)
        val subscription = subscriptionRepository.findByUserId(userId) ?: return
        when (tariff.type) {
            0 -> {
                subscription.mainId = null
                subscription.mainPayDate = null
            }
            else -> {
                subscription.extraId = null
                subscription.extraPayDate = null
            }
        }
        subscriptionRepository.save(subscription.apply { isNew = false })
    }

    private fun createOrUpdateTariff(
        userId: UUID,
        tariff: TariffEntity,
        subscription: SubscriptionEntity?,
        type: Int = tariff.type
    ) {
        if (subscription == null) {
            subscriptionRepository.save(SubscriptionEntity(
                id = UUID.randomUUID(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                userId = userId,
                mainId = if (type == 0) tariff.id else null,
                mainPayDate = if (type == 0) LocalDateTime.now().plusMonths(1) else null,
                extraId = if (type == 1) tariff.id else null,
                extraPayDate = if (type == 1) LocalDateTime.now().plusMonths(1) else null,
            ).apply { isNew = true })
            return
        }
        if (type == 0) {
            if (subscription.mainId == tariff.id)
                return
            subscription.mainId = tariff.id
            subscription.mainPayDate = LocalDateTime.now().plusMonths(1)
        } else if (type == 1) {
            if (subscription.extraId == tariff.id)
                return
            subscription.extraId = tariff.id
            subscription.extraPayDate = LocalDateTime.now().plusMonths(1)
        }
        subscriptionRepository.save(subscription.apply { isNew = false })
    }
}

package ru.nemodev.insightestate.service.subscription

import org.springframework.stereotype.Service
import ru.nemodev.insightestate.entity.SubscriptionEntity
import ru.nemodev.insightestate.entity.TariffEntity
import ru.nemodev.insightestate.repository.SubscriptionRepository
import ru.nemodev.insightestate.service.tariff.TariffService
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface SubscriptionService {
    fun saveTariff(
        userId: UUID,
        tariffId: UUID,
        promoCode: String? = null,
    )

    fun getTariff(
        userId: UUID
    ): List<TariffEntity>

    fun getSubscription(
        userId: UUID
    ): SubscriptionEntity?

    fun removeTariff(
        userId: UUID,
        tariffId: UUID
    )

    fun getPayments(): List<SubscriptionEntity>

    fun updatePaymentDate(payment: SubscriptionEntity)
}

@Service
class SubscriptionServiceImpl (
    private val subscriptionRepository: SubscriptionRepository,
    private val tariffService: TariffService,
) : SubscriptionService {

    override fun saveTariff(
        userId: UUID,
        tariffId: UUID,
        promoCode: String?
    ) {
        val tariff = tariffService.findById(tariffId)
        val subscription = subscriptionRepository.findByUserId(userId)
        createOrUpdateTariff(
            userId = userId,
            tariff = tariff,
            subscription = subscription,
            promoCode = promoCode
        )
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

    override fun getSubscription(
        userId: UUID
    ): SubscriptionEntity? {
        return subscriptionRepository.findByUserId(userId)
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

    override fun getPayments(): List<SubscriptionEntity> {
        return subscriptionRepository.findPaymentsToPay(
            dateStart = LocalDate.now().atStartOfDay(),
            dateEnd = LocalDate.now().plusDays(1).atStartOfDay()
        )
    }

    override fun updatePaymentDate(payment: SubscriptionEntity) {
        if (payment.mainPayDate != null)
            payment.mainPayDate = payment.mainPayDate?.plusMonths(1)
        if (payment.extraPayDate != null)
            payment.extraPayDate = payment.extraPayDate?.plusMonths(1)
        subscriptionRepository.save(payment.apply { isNew = false })
    }

    private fun createOrUpdateTariff(
        userId: UUID,
        tariff: TariffEntity,
        subscription: SubscriptionEntity?,
        type: Int = tariff.type,
        promoCode: String? = null,
    ) {
        var price = if (type == 0) tariff.price else null
        val date = if (type == 0) LocalDateTime.now().plusDays(14) else null
        val date2 = if (type == 1) LocalDateTime.now().plusDays(14) else null
        var price2 = if (type == 0) tariff.price else null

        if (price != null && promoCode != null) {
            price = when (promoCode.uppercase()) {
                "WEBINAR15" -> price.multiply(BigDecimal(0.85))
                "ACTIVE50" -> price.multiply(BigDecimal(0.5))
                else -> price
            }
        }

        if (price2 != null && promoCode != null) {
            price2 = when (promoCode.uppercase()) {
                "WEBINAR15" -> price2.multiply(BigDecimal(0.85))
                "ACTIVE50" -> price2.multiply(BigDecimal(0.5))
                else -> price2
            }
        }

        if (subscription == null) {
            subscriptionRepository.save(SubscriptionEntity(
                id = UUID.randomUUID(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                userId = userId,
                mainId = if (type == 0) tariff.id else null,
                mainPayDate = date,
                mainPayAmount = price,
                extraId = if (type == 1) tariff.id else null,
                extraPayDate = date2,
                extraPayAmount = price2
            ).apply { isNew = true })
            return
        }
        if (type == 0) {
            if (subscription.mainId == tariff.id)
                return
            subscription.mainId = tariff.id
            subscription.mainPayDate = date
            subscription.mainPayAmount = price
            if (subscription.mainId != UUID.fromString("8acf9e68-c4d0-43b1-9c22-b7f712f101a4")) {
                subscription.extraId = null
                subscription.extraPayDate = null
                subscription.extraPayAmount = null
            }
        } else if (type == 1) {
            if (subscription.extraId == tariff.id)
                return
            subscription.extraId = tariff.id
            subscription.extraPayDate = date2
            subscription.extraPayAmount = price2
        }
        subscriptionRepository.save(subscription.apply { isNew = false })
    }
}

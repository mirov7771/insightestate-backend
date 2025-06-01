package ru.nemodev.insightestate.service

import com.stripe.StripeClient
import com.stripe.exception.CardException
import com.stripe.param.CustomerCreateParams
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.PaymentMethodListParams
import com.stripe.param.SubscriptionCreateParams
import jakarta.annotation.PostConstruct
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeRecurrentRq
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeRq
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeRs
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeSubscriptionRq
import ru.nemodev.insightestate.entity.StripeUserEntity
import ru.nemodev.insightestate.repository.StripeUserRepository
import ru.nemodev.insightestate.service.subscription.SubscriptionService
import ru.nemodev.platform.core.logging.sl4j.Loggable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


interface StripeService {
    fun session(rq: StripeRq): StripeRs
    fun subscription(rq: StripeSubscriptionRq)
    fun recurrent(rq: StripeRecurrentRq)
}

@Service
class StripeServiceImpl (
    private val stripeUserRepository: StripeUserRepository,
    private val subscriptionService: SubscriptionService
) : StripeService {

    companion object: Loggable {
        var client: StripeClient = StripeClient("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")
    }

    override fun session(rq: StripeRq): StripeRs {
        val stripeUser = findOrCreate(rq.userId)
        val params =
            PaymentIntentCreateParams.builder()
                .setAmount(rq.amount)
                .setCustomer(stripeUser)
                .setCurrency(rq.currency)
                .setSetupFutureUsage(PaymentIntentCreateParams.SetupFutureUsage.OFF_SESSION)
                .build()
        val paymentIntent = client.paymentIntents().create(params)
        return StripeRs(paymentIntent.clientSecret)
    }

    override fun subscription(rq: StripeSubscriptionRq) {
        val stripeUser = findOrCreate(rq.userId)
        val params = SubscriptionCreateParams.builder()
            .setCustomer(stripeUser)
            .addItem(
                SubscriptionCreateParams.Item.builder()
                    .setPrice(rq.tariffId)
                    .build()
            )
            .build()
        client.subscriptions().create(params)
    }

    override fun recurrent(rq: StripeRecurrentRq) {
        val stripeUser = findOrCreate(rq.userId)
        val params =
            PaymentMethodListParams.builder()
                .setCustomer(stripeUser)
                .setType(PaymentMethodListParams.Type.CARD)
                .build()
        val list = client.paymentMethods().list(params)
        val payment = list.data.sortedByDescending { it.created }.firstOrNull()
        if (payment != null) {
            startRecurrent(rq.userId, stripeUser, payment.id)
        }
    }

    private fun startRecurrent(
        userId: UUID,
        customerId: String,
        paymentId: String
    ) {
        val amount = getAmount(userId)
        if (amount == 0L) {
            return
        }
        val tariffId = getTariff(userId)
        val params =
            PaymentIntentCreateParams.builder()
                .setCurrency("usd")
                .setAmount(amount)
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .setCustomer(customerId)
                .setPaymentMethod(paymentId)
                .setReturnUrl("https://insightestate.pro/tariffs?tariffId=$tariffId")
                .setConfirm(true)
                .setOffSession(true)
                .build()
        try {
            val payment = client.paymentIntents().create(params)
            logger.info("Recurrent Success = ${payment.id}")
        } catch (e: CardException) {
            logger.error("Recurrent Error =", e)
            client.paymentIntents().retrieve(e.stripeError.paymentIntent.id)
        }
    }

    private fun findOrCreate(userId: UUID): String {
        var user = stripeUserRepository.findByUserId(userId)
        if (user == null) {
            val paramsCustomer =
                CustomerCreateParams.builder()
                    .setName(userId.toString())
                    .build()
            val customer = client.customers().create(paramsCustomer)
            user = StripeUserEntity(
                id = userId,
                userId = userId,
                customerId = customer.id
            ).apply { isNew = true }
            stripeUserRepository.save(user)
        }
        return user.customerId
    }

    private fun getAmount(
        userId: UUID
    ): Long {
        val subscription = subscriptionService.getSubscription(userId) ?: return 0L
        val price = subscription.mainPayAmount?.toLong()?.plus(
            (subscription.extraPayAmount ?: BigDecimal.ZERO).toLong()
        )
        return (price ?: 0L) * 100
    }

    private fun getTariff(
        userId: UUID
    ): UUID? {
        return subscriptionService.getSubscription(userId)?.mainId
    }

//    @Scheduled(cron = "0 0 0 * * *")
    fun startPayment() {
        logger.info("Starting recurring payment at {}", LocalDateTime.now())
        subscriptionService.getPayments().forEach {
            logger.info("Starting recurring payment for userId = {}, mainAmount = {}, extraAmount = {}"
                , it.userId, it.mainPayAmount, it.extraPayAmount)
            recurrent(
                rq = StripeRecurrentRq(it.userId)
            )
            subscriptionService.updatePaymentDate(it)
        }
        logger.info("End recurring payment at {}", LocalDateTime.now())
    }
}

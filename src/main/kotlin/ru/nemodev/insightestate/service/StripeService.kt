package ru.nemodev.insightestate.service

import com.stripe.StripeClient
import com.stripe.exception.CardException
import com.stripe.param.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeRecurrentRq
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeRq
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeRs
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeSubscriptionRq
import ru.nemodev.insightestate.config.property.AppProperties
import ru.nemodev.insightestate.entity.StripeUserEntity
import ru.nemodev.insightestate.repository.StripeUserRepository
import ru.nemodev.insightestate.service.subscription.SubscriptionService
import ru.nemodev.platform.core.logging.sl4j.Loggable
import java.time.LocalDateTime
import java.util.*


interface StripeService {
    fun session(rq: StripeRq): StripeRs
    fun subscription(rq: StripeSubscriptionRq)
    fun recurrent(rq: StripeRecurrentRq): Boolean
    fun refund(rq: StripeRecurrentRq)
}

@Service
class StripeServiceImpl (
    private val stripeUserRepository: StripeUserRepository,
    private val subscriptionService: SubscriptionService,
    private val emailService: EmailService,
    private val appProperties: AppProperties
) : StripeService {

    companion object: Loggable {
        var client: StripeClient = StripeClient("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")
    }

    override fun session(rq: StripeRq): StripeRs {
        try {
            val stripeUser = findOrCreate(rq.userId, rq.currency)
            val params =
                PaymentIntentCreateParams.builder()
                    .setAmount(rq.amount)
                    .setCustomer(stripeUser)
                    .setCurrency(rq.currency)
                    .setSetupFutureUsage(PaymentIntentCreateParams.SetupFutureUsage.OFF_SESSION)
                    .build()
            val paymentIntent = client.paymentIntents().create(params)
            return StripeRs(paymentIntent.clientSecret)
        } catch (e: Exception) {
            sendNotification(rq, e)
            logError {"Ошибка создания сессия для оплаты, userId = ${rq.userId}"}
            throw e
        }
    }

    private fun sendNotification(rq: StripeRq, e: Exception) {
        val message = buildString {
            appendLine("Ошибка создания paymentIntents для оплаты в stripe")
            appendLine()
            appendLine("userId: ${rq.userId}")
            appendLine("amount: ${rq.amount}")
            appendLine("currency: ${rq.currency}")
            appendLine()
            appendLine("Exception: ${e::class.qualifiedName}")
            appendLine("Message: ${e.message}")
        }

        emailService.sendMessage(
            email = appProperties.developerEmail,
            subject = "Stripe error: userId=${rq.userId}",
            message = message
        )
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

    override fun recurrent(rq: StripeRecurrentRq): Boolean {
        val paymentId = getPaymentId(rq.userId)
        if (paymentId != null) {
            return startRecurrent(rq.userId, findOrCreate(rq.userId), paymentId, getCurrency(rq.userId))
        }
        return false
    }

    override fun refund(rq: StripeRecurrentRq) {
        Thread.sleep(5000)
        val paymentId = getPaymentId(rq.userId, "charge") ?: return
        val params = RefundCreateParams.builder()
            .setCharge(paymentId)
            .build()
        val rs = client.refunds().create(params)
        try {
            logger.info("Refund Success = {}", rs.id)
        } catch (e: Exception) {
            logger.error("Refund Error =", e)
        }
    }

    private fun getPaymentId(
        userId: UUID,
        type: String = "method"
    ): String? {
        val stripeUser = findOrCreate(userId)
        return when (type) {
            "intent" -> {
                val params = PaymentIntentListParams.builder()
                    .setCustomer(stripeUser)
                    .build()
                val list = client.paymentIntents().list(params)
                val payment = list.data.sortedByDescending { it.created }.firstOrNull()
                payment?.id
            }
            "charge" -> {
                val params = ChargeListParams.builder()
                    .setCustomer(stripeUser)
                    .build()
                var list = client.charges().list(params)
                var payment = list.data.filter { !it.refunded }.sortedByDescending { it.created }.firstOrNull()
                var id = payment?.id
                if (id == null) {
                   for (i in 0..10) {
                       list = client.charges().list(params)
                       payment = list.data.filter { !it.refunded && it.amount > 200 }.sortedByDescending { it.created }.firstOrNull()
                       id = payment?.id
                       if (id != null) {
                           break
                       } else {
                           Thread.sleep(3000)
                       }
                   }
                }
                id
            }
            else -> {
                val params =
                    PaymentMethodListParams.builder()
                        .setCustomer(stripeUser)
                        .setType(PaymentMethodListParams.Type.CARD)
                        .build()
                val list = client.paymentMethods().list(params)
                val payment = list.data.sortedByDescending { it.created }.firstOrNull()
                payment?.id
            }
        }
    }

    private fun startRecurrent(
        userId: UUID,
        customerId: String,
        paymentId: String,
        currency: String
    ): Boolean {
        val amount = getAmount(userId)
        if (amount == 0L) {
            return false
        }
        val tariffId = getTariff(userId)
        val params =
            PaymentIntentCreateParams.builder()
                .setCurrency(currency)
                .setAmount(amount)
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .setCustomer(customerId)
                .setPaymentMethod(paymentId)
                .setReturnUrl("https://lotsof.properties/tariffs?tariffId=$tariffId")
                .setConfirm(true)
                .setOffSession(true)
                .build()
        try {
            val payment = client.paymentIntents().create(params)
            logger.info("Recurrent Success = ${payment.id}")
            return true
        } catch (e: CardException) {
            logger.error("Recurrent Error =", e)
            client.paymentIntents().retrieve(e.stripeError.paymentIntent.id)
            return false
        }
    }

    private fun findOrCreate(
        userId: UUID,
        currency: String? = null,
    ): String {
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
                customerId = customer.id,
                currency = currency ?: "usd",
            ).apply { isNew = true }
            stripeUserRepository.save(user)
        }
        return user.customerId
    }

    private fun getCurrency(userId: UUID): String {
        return stripeUserRepository.findByUserId(userId)?.currency!!
    }

    private fun getAmount(
        userId: UUID
    ): Long {
        val subscription = subscriptionService.getSubscription(userId) ?: return 0L
        val price = subscription.mainPayAmount?.toLong()
        return (price ?: 0L) * 100
    }

    private fun getTariff(
        userId: UUID
    ): UUID? {
        return subscriptionService.getSubscription(userId)?.mainId
    }

    @Scheduled(cron = "0 0 0 * * *")
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

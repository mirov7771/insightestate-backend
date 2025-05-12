package ru.nemodev.insightestate.service

import com.stripe.StripeClient
import com.stripe.param.CustomerCreateParams
import com.stripe.param.PaymentIntentCreateParams
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeRq
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeRs
import ru.nemodev.insightestate.entity.StripeUserEntity
import ru.nemodev.insightestate.repository.StripeUserRepository
import java.util.UUID


interface StripeService {
    fun session(rq: StripeRq): StripeRs
}

@Service
class StripeServiceImpl (
    private val stripeUserRepository: StripeUserRepository
) : StripeService {

    companion object {
        var client: StripeClient = StripeClient("sk_test_51RHea2C7cCHxCxhsgW936lT7lNCfUVtradcYJ21ttFTaMcBc8tWn8qx4yPIZoeIWNeFPNsymy6j3G5dx2LBNjMw4000lp7RWwP")
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

    fun findOrCreate(userId: UUID): String {
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
}

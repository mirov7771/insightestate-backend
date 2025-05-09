package ru.nemodev.insightestate.service

import com.stripe.StripeClient
import com.stripe.param.PaymentIntentCreateParams
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeRq
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeRs


interface StripeService {
    fun session(rq: StripeRq): StripeRs
}

@Service
class StripeServiceImpl : StripeService {

    companion object {
        var client: StripeClient = StripeClient("sk_test_51RHea2C7cCHxCxhsgW936lT7lNCfUVtradcYJ21ttFTaMcBc8tWn8qx4yPIZoeIWNeFPNsymy6j3G5dx2LBNjMw4000lp7RWwP")
    }

    override fun session(rq: StripeRq): StripeRs {
        val params =
            PaymentIntentCreateParams.builder()
                .setAmount(rq.amount)
                .setCurrency(rq.currency)
                .setSetupFutureUsage(PaymentIntentCreateParams.SetupFutureUsage.OFF_SESSION)
                .build()
        val paymentIntent = client.paymentIntents().create(params)
        return StripeRs(paymentIntent.clientSecret)
    }
}

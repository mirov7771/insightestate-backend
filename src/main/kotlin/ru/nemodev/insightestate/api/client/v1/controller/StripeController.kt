package ru.nemodev.insightestate.api.client.v1.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.core.parameters.P
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeRecurrentRq
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeRq
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeRs
import ru.nemodev.insightestate.api.client.v1.dto.stripe.StripeSubscriptionRq
import ru.nemodev.insightestate.service.StripeService

@RestController
@RequestMapping(value = ["/api/v1/stripe", "/v1/stripe"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Объекты строек")
class StripeController (
    private val stripeService: StripeService
) {
    @PostMapping("/session")
    fun session(
        @RequestBody
        rq: StripeRq
    ): StripeRs = stripeService.session(rq)

    @PostMapping("/subscription")
    fun subscription(
        @RequestBody
        rq: StripeSubscriptionRq
    ) = stripeService.subscription(rq)

    @PostMapping("/recurrent")
    fun recurrent(
        @RequestBody
        rq: StripeRecurrentRq
    ) = stripeService.recurrent(rq)
}

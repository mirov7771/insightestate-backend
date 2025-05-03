package ru.nemodev.insightestate.api.client.v1.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ru.nemodev.insightestate.api.client.v1.dto.subscription.SubscriptionRq
import ru.nemodev.insightestate.api.client.v1.processor.SubscriptionProcessor
import java.util.*

@RestController
@RequestMapping(value = ["/api/v1/subscription", "/v1/subscription"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Тарифы")
class SubscriptionController (
    private val processor: SubscriptionProcessor
) {
    @PostMapping
    fun saveTariff(
        @RequestBody
        rq: SubscriptionRq
    ) = processor.saveTariff(rq)

    @GetMapping
    fun getTariff(
        @RequestParam
        userId: UUID
    ) = processor.getTariff(userId)

    @DeleteMapping
    fun removeTariff(
        @RequestBody
        rq: SubscriptionRq
    ) = processor.removeTariff(rq)
}

package ru.nemodev.insightestate.api.client.v1.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nemodev.insightestate.api.client.v1.dto.tariff.TariffRs
import ru.nemodev.insightestate.api.client.v1.processor.TariffProcessor

@RestController
@RequestMapping(value = ["/api/v1/tariff", "/v1/tariff"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Тарифы")
class TariffController (
    private val processor: TariffProcessor
) {
    @GetMapping
    fun findAll(): TariffRs = processor.findAll()
}

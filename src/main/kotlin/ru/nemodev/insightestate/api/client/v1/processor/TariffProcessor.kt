package ru.nemodev.insightestate.api.client.v1.processor

import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.converter.TariffConverter
import ru.nemodev.insightestate.api.client.v1.dto.tariff.TariffRs
import ru.nemodev.insightestate.service.tariff.TariffService

interface TariffProcessor {
    fun findAll(): TariffRs
}

@Component
class TariffProcessorImpl (
    private val tariffService: TariffService,
    private val converter: TariffConverter,
) : TariffProcessor {
    override fun findAll(): TariffRs {
        val entities = tariffService.findAll()
        return TariffRs(
            main = entities.filter { it.type == 0 }.map { converter.convert(it) },
            extra = entities.filter { it.type == 1 }.map { converter.convert(it) }
        )
    }
}

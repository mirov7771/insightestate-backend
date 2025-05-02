package ru.nemodev.insightestate.api.client.v1.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.dto.tariff.TariffDto
import ru.nemodev.insightestate.entity.TariffEntity

@Component
class TariffConverter: Converter<TariffEntity, TariffDto> {
    override fun convert(source: TariffEntity): TariffDto {
        return TariffDto(
            id = source.id,
            title = source.title,
            price = source.price,
            description = if (source.description.contains(";"))
                source.description.split(";")
            else
                listOf(source.description)
        )
    }
}

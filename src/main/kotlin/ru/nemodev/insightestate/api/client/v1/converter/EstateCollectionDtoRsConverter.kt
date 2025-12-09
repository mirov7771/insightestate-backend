package ru.nemodev.insightestate.api.client.v1.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateCollectionDtoRs
import ru.nemodev.insightestate.domen.EstateCollection

@Component
class EstateCollectionDtoRsConverter(
    private val estateDetailDtoRsConverter: EstateDetailDtoRsConverter
) : Converter<EstateCollection, EstateCollectionDtoRs> {

    override fun convert(source: EstateCollection): EstateCollectionDtoRs {
        return EstateCollectionDtoRs(
            id = source.estateCollection.id,
            name = source.estateCollection.collectionDetail.name,
            estates = source.estates.map { estateDetailDtoRsConverter.convert(it) }.ifEmpty { null },
            comment = source.estateCollection.collectionDetail.comment,
            archive = source.estateCollection.collectionDetail.archive,
        )
    }
}

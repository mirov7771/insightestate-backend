package ru.nemodev.insightestate.service.airtable

import org.springframework.stereotype.Component
import ru.nemodev.insightestate.entity.UnitEntity
import ru.nemodev.insightestate.extension.toRoundedString
import ru.nemodev.insightestate.integration.airtable.dto.UnitRecordsDtoRs.AirtableRecordDto.AirtableProjectFieldsDto

interface UnitAirtableMapper {
    fun mapToEntity(airtableDto: AirtableProjectFieldsDto, entity: UnitEntity?): UnitEntity
}

@Component
class UnitAirtableMapperImpl : UnitAirtableMapper {

    companion object {
        const val BASE_IMAGE_URL = "https://lotsof.properties/estate-images/"
    }

    override fun mapToEntity(
        airtableDto: AirtableProjectFieldsDto,
        entity: UnitEntity?
    ): UnitEntity {

        return if (entity == null) {
            UnitEntity(
                code = airtableDto.unitId,
                projectId = airtableDto.projectIds?.get(0),
                corpus = airtableDto.corpus,
                number = airtableDto.unitNumber,
                floor = airtableDto.floor,
                rooms = airtableDto.rooms,
                square = airtableDto.square?.toRoundedString(),
                priceSq = airtableDto.pricePerM2?.toRoundedString(),
                price = airtableDto.totalPrice?.toRoundedString(),
                planImage = airtableDto.layoutCode?.let { "$BASE_IMAGE_URL$it.webp" }
            )
        } else {
            entity.code = airtableDto.unitId
            entity.projectId = airtableDto.projectIds?.get(0)
            entity.corpus = airtableDto.corpus
            entity.number = airtableDto.unitNumber
            entity.floor = airtableDto.floor
            entity.rooms = airtableDto.rooms
            entity.square = airtableDto.square?.toRoundedString()
            entity.priceSq = airtableDto.pricePerM2?.toRoundedString()
            entity.price = airtableDto.totalPrice?.toRoundedString()
            entity.planImage = airtableDto.layoutCode?.let { "$BASE_IMAGE_URL$it.webp" }

            return entity
        }
    }
}

package ru.nemodev.insightestate.api.client.v1.dto.estate

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Запрос создания коллекции объектов")
data class EstateCollectionCreateDtoRq(
    @Schema(description = "Имя", example = "Топ подборка")
    val name: String,

    @Schema(description = "Id объекта")
    val estateId: UUID?,
)

@Schema(description = "Ответ создания коллекции объектов")
data class EstateCollectionCreateDtoRs(
    @Schema(description = "Id")
    val id: UUID,
)

@Schema(description = "Ответ элемента списка коллекция объектов")
data class EstateCollectionDtoRs(
    @Schema(description = "Id")
    val id: UUID,

    @Schema(description = "Имя", example = "Топ подборка")
    val name: String,

    @Schema(description = "Детальная информация по объекту")
    val estates: List<EstateDetailDtoRs>?
)

data class EstateCollectionUpdateDto (
    val name: String
)

data class ShortDto (
    val url: String,
)

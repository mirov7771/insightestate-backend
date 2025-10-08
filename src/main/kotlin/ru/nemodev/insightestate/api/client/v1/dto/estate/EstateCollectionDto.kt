package ru.nemodev.insightestate.api.client.v1.dto.estate

import io.swagger.v3.oas.annotations.media.Schema
import ru.nemodev.insightestate.api.client.v1.dto.user.UserDtoRs
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
    val estates: List<EstateDetailDtoRs>?,

    var agentInfo: UserDtoRs? = null
)

data class EstateCollectionUpdateDto (
    val name: String
)

data class ShortDto (
    val url: String,
)

data class LikeDto (
    val estateId: UUID,
    val collectionId: UUID,
    val title: String,
    val email: String,
    val collection: String,
    val url: String,
)

data class TemplateRq (
    val id: Int,
    val userId: UUID,
    val template: String,
)

data class TemplateRs (
    val id: UUID
)

data class DuplicateRq (
    val id: UUID,
)

data class ActivityDto (
    val id: UUID,
    val url: String,
)

package ru.nemodev.insightestate.api.client.v1.dto.user

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*

@Schema(description = "Ответ информация по пользователю")
data class UserDtoRs(
    val id: UUID,
    @Schema(description = "Email", example = "test@gmail.com")
    val login: String,

    @Schema(description = "ФИО", example = "Ivanov Ivan")
    val fio: String,

    @Schema(description = "Номер телефона", example = "+79531234567")
    val mobileNumber: String,

    @Schema(description = "Страна и город", example = "Kazahstan Astana")
    val location: String,

    @Schema(description = "WhatsUp", example = "1234567890")
    val whatsUp: String?,

    @Schema(description = "Telegram", example = "1234567890")
    val tgName: String?,

    @Schema(description = "Фото профиля", example = "1234567890")
    val profileImage: String?,

    val group: String?,

    val collectionLogo: String?,
    val collectionColorId: String?,
    val collectionColorValue: String?,

    val collectionCount: Int?,
)

@Schema(description = "Запрос обновления пользователя")
data class UserUpdateDtoRq(
    @Schema(description = "ФИО", example = "Ivanov Ivan")
    @field:NotBlank
    @field:Size(min = 1, max = 128, message = "Field must contains from 1 to 128 characters")
    val fio: String,

    @Schema(description = "Номер телефона", example = "+79531234567")
    @field:NotBlank
    val mobileNumber: String,

    @Schema(description = "Страна и город", example = "Kazahstan Astana")
    @field:NotBlank
    val location: String,

    @Schema(description = "Пароль", example = "1234567890")
    val password: String?,

    @Schema(description = "WhatsUp", example = "1234567890")
    val whatsUp: String?,

    @Schema(description = "Telegram", example = "1234567890")
    val tgName: String?,

    @Schema(description = "Фото профиля", example = "1234567890")
    val profileImage: String?,
)

data class UserGroupDto (
    val email: String,
    val group: Group? = null,
    val tariff: Tariff? = null
)

enum class Group {
    extra,
    insightestate,
    comfort,
    SID,
    neginski
}

enum class Tariff {
    Start,
    Pro,
    Enterpise
}

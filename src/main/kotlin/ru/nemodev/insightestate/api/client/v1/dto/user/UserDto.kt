package ru.nemodev.insightestate.api.client.v1.dto.user

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Ответ информация по пользователю")
data class UserDtoRs(
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
)

@Schema(description = "Запрос обновления пользователя")
data class UserUpdateDtoRq(
    @Schema(description = "ФИО", example = "Ivanov Ivan")
    @field:NotBlank
    @field:Size(min = 1, max = 128, message = "Field must contains from 1 to 128 characters")
    val fio: String,

    @Schema(description = "Номер телефона", example = "+79531234567")
    @field:NotBlank
    @field:Size(min = 10, max = 32, message = "Field must contains from 10 to 32 characters")
    val mobileNumber: String,

    @Schema(description = "Страна и город", example = "Kazahstan Astana")
    @field:NotBlank
    @field:Size(min = 4, max = 64, message = "Field must contains from 4 to 64 characters")
    val location: String,

    @Schema(description = "Пароль", example = "1234567890")
    @field:Size(min = 6, max = 15, message = "Field must contains from 6 to 15 characters")
    val password: String?,
    
    @Schema(description = "WhatsUp", example = "1234567890")
    val whatsUp: String?,

    @Schema(description = "Telegram", example = "1234567890")
    val tgName: String?,

    @Schema(description = "Фото профиля", example = "1234567890")
    val profileImage: String?,
)

package ru.nemodev.insightestate.api.auth.v1.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Запрос регистрации")
data class SignUpDtoRq(
    @Schema(description = "Email", example = "test@gmail.com")
    var login: String,
)

@Schema(description = "Запрос проверки кода")
data class SignUpConfirmCodeDtoRq(
    @Schema(description = "Email", example = "test@gmail.com")
    var login: String,

    @Schema(description = "Код подтверждения почты", example = "123456")
    val confirmCode: String,
)

@Schema(description = "Запрос завершения регистрации")
data class SignUpEndDtoRq(
    @Schema(description = "Email", example = "test@gmail.com")
    var login: String,

    @Schema(description = "ФИО", example = "Ivanov Ivan")
    val fio: String,

    @Schema(description = "Номер телефона", example = "+79531234567")
    val mobileNumber: String,

    @Schema(description = "Страна и город", example = "Kazahstan Astana")
    val location: String,

    @Schema(description = "Пароль", example = "1234567890")
    val password: String,

    @Schema(description = "WhatsUp", example = "1234567890")
    val whatsUp: String?,

    @Schema(description = "Telegram", example = "1234567890")
    val tgName: String?,

    @Schema(description = "Фото профиля", example = "1234567890")
    val profileImage: String?,
)

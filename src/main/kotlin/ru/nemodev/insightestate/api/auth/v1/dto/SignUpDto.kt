package ru.nemodev.insightestate.api.auth.v1.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Запрос регистрации")
data class SignUpDtoRq(
    @Schema(description = "Email", example = "test@gmail.com")
    @field:Pattern(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})\$", message = "Please input correct email")
    val login: String,
)

@Schema(description = "Запрос проверки кода")
data class SignUpConfirmCodeDtoRq(
    @Schema(description = "Email", example = "test@gmail.com")
    @field:Pattern(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})\$", message = "Please input correct email")
    val login: String,

    @Schema(description = "Код подтверждения почты", example = "123456")
    @field:NotBlank
    @field:Size(min = 6, max = 6, message = "Field must contains 6 characters")
    val confirmCode: String,
)

@Schema(description = "Запрос завершения регистрации")
data class SignUpEndDtoRq(
    @Schema(description = "Email", example = "test@gmail.com")
    @field:Pattern(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})\$", message = "Please input correct email")
    val login: String,

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
    @field:NotBlank
    @field:Size(min = 6, max = 15, message = "Field must contains from 6 to 15 characters")
    val password: String,
)

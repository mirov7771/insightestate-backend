package ru.nemodev.insightestate.api.auth.v1.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Запрос сброса пароля")
data class UserPasswordResetDtoRq(
    @Schema(description = "Логин(email)")
    var login: String
)

@Schema(description = "Запрос сброса пароля")
data class UserPasswordResetConfirmDtoRq(
    @Schema(description = "Логин(email)")
    var login: String,

    @Schema(description = "Код подтверждения сброса пароля")
    val confirmCode: String,

    @Schema(description = "Новый пароль")
    val newPassword: String
)

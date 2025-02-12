package ru.nemodev.insightestate.api.auth.v1.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Токен авторизации")
data class SignInDtoRs(
    @Schema(description = "access token")
    val accessToken: String,

    @Schema(description = "refresh token")
    val refreshToken: String,

    @Schema(description = "Тип токена")
    val tokenType: String,

    @Schema(description = "Время жизни токена")
    val expiresIn: Long
)
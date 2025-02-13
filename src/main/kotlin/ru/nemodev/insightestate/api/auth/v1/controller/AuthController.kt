package ru.nemodev.insightestate.api.auth.v1.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ru.nemodev.insightestate.api.auth.v1.dto.SignInDtoRs
import ru.nemodev.insightestate.api.auth.v1.dto.SignUpConfirmCodeDtoRq
import ru.nemodev.insightestate.api.auth.v1.dto.SignUpDtoRq
import ru.nemodev.insightestate.api.auth.v1.dto.SignUpEndDtoRq
import ru.nemodev.insightestate.service.security.AuthService
import ru.nemodev.platform.core.api.dto.error.ErrorDtoRs

@RestController
@RequestMapping("/auth", produces = [MediaType.APPLICATION_JSON_VALUE])
@SecurityScheme(
    name = "basicAuth",
    scheme = "basic",
    type = SecuritySchemeType.HTTP,
    `in` = SecuritySchemeIn.HEADER
)
class AuthController (
    private val authService: AuthService
) {

    @Operation(
        summary = "Регистрация",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "401", description = "Ошибка авторизации",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "409", description = "Пользователь уже зарегестрирован",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "422", description = "Ошибка валидации",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            )
        ]
    )
    @PostMapping("/sign-up")
    fun signUp(
        @RequestBody
        @Valid
        request: SignUpDtoRq
    ) {
        authService.signUp(request)
    }

    @Operation(
        summary = "Регистрация - проверка кода",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "404", description = "Пользователь не найден",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "422", description = "Ошибка валидации",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            )
        ]
    )
    @PostMapping("/sign-up/confirm-code/check")
    fun signUpConfirmCodeCheck(
        @RequestBody
        @Valid
        request: SignUpConfirmCodeDtoRq
    ) {
        authService.signUpCheckConfirmCode(request)
    }

    @Operation(
        summary = "Регистрация - повторная отправка кода",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "404", description = "Пользователь не найден",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "422", description = "Ошибка валидации",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            )
        ]
    )
    @PostMapping("/sign-up/confirm-code/new")
    fun signUpConfirmCodeNew(
        @RequestBody
        @Valid
        request: SignUpDtoRq
    ) {
        authService.signUpSendNewConfirmCode(request)
    }

    @Operation(
        summary = "Регистрация - завершение",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "404", description = "Пользователь не найден",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "422", description = "Ошибка валидации",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            )
        ]
    )
    @PostMapping("/sign-up/end")
    fun signUpEnd(
        @RequestBody
        @Valid
        request: SignUpEndDtoRq
    ) {
        authService.signUpEnd(request)
    }

    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "Авторизация (получение JWT токена)",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "401", description = "Ошибка авторизации",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            )
        ]
    )
    @PostMapping("/sign-in")
    fun signIn(
        @Parameter(description = "Токен basic auth", required = true, hidden = true)
        @RequestHeader("Authorization") authBasicToken: String
    ): SignInDtoRs {
        return authService.signIn(authBasicToken)
    }
}

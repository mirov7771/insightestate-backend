package ru.nemodev.insightestate.api.client.v1.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ru.nemodev.insightestate.api.client.v1.dto.user.UserDtoRs
import ru.nemodev.insightestate.api.client.v1.dto.user.UserUpdateDtoRq
import ru.nemodev.insightestate.service.UserService
import ru.nemodev.platform.core.api.dto.error.ErrorDtoRs

@RestController
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController (
    private val userService: UserService
) {

    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "Получение пользователя",
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
    @GetMapping("/me")
    fun getUser(
        @Parameter(description = "Токен basic auth", required = true, hidden = true)
        @RequestHeader("Authorization") authBasicToken: String,
    ): UserDtoRs {
        return userService.getUser(authBasicToken)
    }

    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "Обновление информации пользователя",
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
    @PutMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateUser(
        @Parameter(description = "Токен basic auth", required = true, hidden = true)
        @RequestHeader("Authorization") authBasicToken: String,

        @Valid
        @RequestBody
        request: UserUpdateDtoRq
    ) {
        userService.update(authBasicToken, request)
    }
}

package ru.nemodev.insightestate.api.client.v1.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateCollectionCreateDtoRq
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateCollectionCreateDtoRs
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateCollectionDtoRs
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateCollectionUpdateDto
import ru.nemodev.insightestate.api.client.v1.processor.EstateCollectionProcessor
import ru.nemodev.platform.core.api.dto.error.ErrorDtoRs
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
import java.util.*

@RestController
@RequestMapping(value = ["/api/v1/estate-collections", "/v1/estate-collections"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Коллекции объектов строек", description = "Во всех запросах требуется заголовок Authorization: Basic Auth")
@SecurityRequirement(name = "basicAuth")
class EstateCollectionController (
    private val estateCollectionProcessor: EstateCollectionProcessor
) {

    @Operation(
        summary = "Список",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "401", description = "Не авторизованный запрос",
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
    @GetMapping
    fun findAll(
        @Parameter(description = "Токен basic auth", required = true, hidden = true)
        @RequestHeader("Authorization") authBasicToken: String,

        @Parameter(description = "Номер страницы", example = "0", required = false)
        @RequestParam(name = "pageNumber", required = false)
        @Valid
        @Min(0, message = "Минимальное значение 0")
        pageNumber: Int? = 0,

        @Parameter(description = "Размер страницы", example = "25", required = false)
        @RequestParam(name = "pageSize", required = false)
        @Valid
        @Min(1, message = "Минимальное значение 1")
        @Max(100, message = "Максимальное значение 100")
        pageSize: Int? = 25
    ): PageDtoRs<EstateCollectionDtoRs> = estateCollectionProcessor.findAll(
        authBasicToken = authBasicToken,
        pageable = PageRequest.of(
            pageNumber ?: 0,
            pageSize ?: 25
        )
    )

    @Operation(
        summary = "Создать",
        responses = [
            ApiResponse(responseCode = "201", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "401", description = "Не авторизованный запрос",
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
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun create(
        @Parameter(description = "Токен basic auth", required = true, hidden = true)
        @RequestHeader("Authorization") authBasicToken: String,

        @RequestBody
        request: EstateCollectionCreateDtoRq
    ): EstateCollectionCreateDtoRs {
        return estateCollectionProcessor.create(authBasicToken, request)
    }

    @Operation(
        summary = "Добавить объект в коллекцию",
        responses = [
            ApiResponse(responseCode = "204", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "401", description = "Не авторизованный запрос",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "403", description = "Не достаточно прав для исполнения запроса",
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{id}/estate")
    fun addEstateToCollection(
        @Parameter(description = "Токен basic auth", required = true, hidden = true)
        @RequestHeader("Authorization") authBasicToken: String,

        @Parameter(description = "Id коллекции", required = true)
        @PathVariable("id") id: UUID,

        @Parameter(description = "Id объекта", required = true)
        @RequestParam("estateId") estateId: UUID
    ) {
        estateCollectionProcessor.addEstateToCollection(
            authBasicToken = authBasicToken,
            id = id,
            estateId = estateId
        )
    }

    @Operation(
        summary = "Удалить объект из коллекции",
        responses = [
            ApiResponse(responseCode = "204", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "401", description = "Не авторизованный запрос",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "403", description = "Не достаточно прав для исполнения запроса",
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}/estate")
    fun deleteEstateFromCollection(
        @Parameter(description = "Токен basic auth", required = true, hidden = true)
        @RequestHeader("Authorization") authBasicToken: String,

        @Parameter(description = "Id коллекции", required = true)
        @PathVariable("id") id: UUID,

        @Parameter(description = "Id объекта", required = true)
        @RequestParam("estateId") estateId: UUID
    ) {
        estateCollectionProcessor.deleteEstateFromCollection(
            authBasicToken = authBasicToken,
            id = id,
            estateId = estateId
        )
    }

    @Operation(
        summary = "Удалить коллекцию",
        responses = [
            ApiResponse(responseCode = "204", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "401", description = "Не авторизованный запрос",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "403", description = "Не достаточно прав для исполнения запроса",
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteById(
        @Parameter(description = "Токен basic auth", required = true, hidden = true)
        @RequestHeader("Authorization") authBasicToken: String,

        @Parameter(description = "Id коллекции", required = true)
        @PathVariable("id") id: UUID
    ) {
        estateCollectionProcessor.deleteById(
            authBasicToken = authBasicToken,
            id = id
        )
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable("id") id: UUID
    ): EstateCollectionDtoRs = estateCollectionProcessor.getById(id)

    @PutMapping("/{id}")
    fun update(
        @PathVariable("id") id: UUID,
        @RequestBody rq: EstateCollectionUpdateDto
    ) = estateCollectionProcessor.update(id, rq)
}

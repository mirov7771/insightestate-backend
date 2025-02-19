package ru.nemodev.insightestate.api.client.v1.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDetailDtoRs
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDtoRs
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.insightestate.service.estate.EstateService
import ru.nemodev.platform.core.api.dto.error.ErrorDtoRs
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
import java.util.*

@RestController
@RequestMapping("/v1/estate", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Объекты строек")
class EstateController (
    private val service: EstateService
) {

    @Operation(
        summary = "Список",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
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
        @Parameter(description = "Тип застройки", required = false)
        @RequestParam(name = "types", required = false)
        types: Set<EstateType>?,

        @Parameter(description = "Год сдачи объекта / 2025 / 2026 / etc", required = false)
        @RequestParam(name = "buildEndYears", required = false)
        buildEndYears: Set<Int>?,

        @Parameter(description = "Комнаты / 0 - студия / 1 - 1br / 2 - 2br / 3 - 3br / 4 - 4br+", required = false)
        @RequestParam(name = "rooms", required = false)
        rooms: Set<String>?,

        @Parameter(description = "Стоимость / 1 - до 100 000 / 2 - \$100 000 — \$200 000 / 3 - \$200 000 — \$500 000 / 4 - \$500 000 — \$1 000 000 / 5 - от \$1 000 000", required = false)
        @RequestParam(name = "price", required = false)
        price: String?,

        @Parameter(description = "Комфорт и инвестиционный потенциал / 1 - Самые безопасные для инвестиций / 2 - Наибольшая доходность / 3 - Самые удобные локации / 4 - Самые комфортные для жизни", required = false)
        @RequestParam(name = "grades", required = false)
        grades: Set<String>?,

        @Parameter(description = "Время до пляжа / 1 - Менее 5 мин пешком / 2 - 6-10 мин пешком / 3 - 11-30 мин пешком / 11 - Менее 5 мин на машине / 12 - 6-10 мин на машине / 13 - 11-30 мин на машине", required = false)
        @RequestParam(name = "beachTravelTimes", required = false)
        beachTravelTimes: Set<String>?,

        @Parameter(description = "Время до аэропорта / 1 - до 30 мин на машине / 2 - до 60 мин на машине / 3 - 60+ мин на машине", required = false)
        @RequestParam(name = "airportTravelTimes", required = false)
        airportTravelTimes: Set<String>?,

        @Parameter(description = "Наличие парковки", required = false)
        @RequestParam(name = "parking", required = false)
        parking: Boolean?,

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
    ): PageDtoRs<EstateDtoRs> = service.findAll(
        types = types,
        buildEndYears = buildEndYears,
        rooms = rooms,
        price = price,
        grades = grades,
        beachTravelTimes = beachTravelTimes,
        airportTravelTimes = airportTravelTimes,
        parking = parking,
        pageable = PageRequest.of(
            pageNumber ?: 0,
            pageSize ?: 25
        )
    )

    @Operation(
        summary = "Детали",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            )
        ]
    )
    @GetMapping("/{id}")
    fun findById(
        @PathVariable
        id: UUID,
    ): EstateDetailDtoRs = service.findById(id)
}

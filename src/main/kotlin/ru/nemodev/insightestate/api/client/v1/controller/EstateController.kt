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
import org.springframework.web.multipart.MultipartFile
import ru.nemodev.insightestate.api.auth.v1.dto.CustomPageDtoRs
import ru.nemodev.insightestate.api.client.v1.dto.estate.AiRequest
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDetailDtoRs
import ru.nemodev.insightestate.api.client.v1.dto.estate.UnitsRs
import ru.nemodev.insightestate.api.client.v1.dto.user.MainInfoDto
import ru.nemodev.insightestate.api.client.v1.processor.EstateProcessor
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.platform.core.api.dto.error.ErrorDtoRs
import java.math.BigDecimal
import java.util.*

@RestController
@RequestMapping(value = ["/api/v1/estate", "/v1/estate"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Объекты строек")
class EstateController (
    private val estateProcessor: EstateProcessor
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
        @RequestHeader("Currency", required = false)
        currency: String? = null,
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

        @Parameter(description = "Наличие управляющей компании", required = false)
        @RequestParam(name = "managementCompanyEnabled", required = false)
        managementCompanyEnabled: Boolean?,

        @Parameter(description = "Название пляжа, ищется без учета регистра ilike beachName%", required = false)
        @RequestParam(name = "beachName", required = false)
        beachName: Set<String>?,

        @Parameter(description = "Название города, ищется без учета регистра ilike city%", required = false)
        @RequestParam(name = "city", required = false)
        city: Set<String>?,

        @RequestParam(name = "minPrice", required = false)
        minPrice: BigDecimal? = null,

        @RequestParam(name = "maxPrice", required = false)
        maxPrice: BigDecimal? = null,

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
        pageSize: Int? = 25,

        @RequestHeader(name = "x-user-id", required = false)
        userId: UUID? = null,

        @RequestParam(required = false)
        name: String? = null,

        @RequestParam(name = "developer", required = false)
        developer: Set<String>?,
        @RequestParam(name = "petFriendly", required = false)
        petFriendly: Boolean?,
    ): CustomPageDtoRs = estateProcessor.findAll(
        currency = currency,
        types = types,
        buildEndYears = buildEndYears,
        rooms = rooms,
        price = price,
        grades = grades,
        beachTravelTimes = beachTravelTimes,
        airportTravelTimes = airportTravelTimes,
        parking = parking,
        managementCompanyEnabled = managementCompanyEnabled,
        beachName = beachName,
        city = city,
        minPrice = minPrice,
        maxPrice = maxPrice,
        pageable = PageRequest.of(
            pageNumber ?: 0,
            pageSize ?: 25
        ),
        userId = userId,
        name = name,
        developer = developer,
        petFriendly = petFriendly,
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
        @RequestHeader("Currency", required = false)
        currency: String? = null,
        @PathVariable
        id: UUID,
    ): EstateDetailDtoRs = estateProcessor.findById(
        currency = currency,
        id = id
    )

    // TODO вынести эти методы ниже в admin контроллер и закрыть апикеем
    @Operation(
        summary = "Загрузить объекты из excel файла, существующие объекты обновляются с сохранением картинок",
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
    @PostMapping("/load", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun loadFromFile(
        @RequestPart("file")
        filePart: MultipartFile
    ) = estateProcessor.loadFromFile(filePart)

    @Operation(
        summary = "Загрузить объекты из google spreadsheets, существующие объекты обновляются с сохранением картинок",
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
    @PostMapping("/load/google-spreadsheets")
    fun loadFromGoogleSpreadsheets() = estateProcessor.loadFromGoogle()

    @Operation(
        summary = "Загрузить фото объектов из директории, все фото обновляются, процесс загрузки не быстрый т.к фото много",
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
    @PostMapping("/load/images")
    fun loadImageFromGoogleDrive() = estateProcessor.loadImagesFromDir()

    @Operation(
        summary = "Загрузить фото объектов из google drive, все фото обновляются, процесс загрузки не быстрый т.к фото много",
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
    @PostMapping("/load/images/google-drive")
    fun loadImagesFromGoogleDrive() = estateProcessor.loadImagesFromGoogleDrive()

    @PostMapping("ai")
    fun aiRequest(
        @RequestHeader("Currency", required = false)
        currency: String? = null,
        @RequestBody
        rq: AiRequest
    ):CustomPageDtoRs = estateProcessor.aiRequest(
        currency = currency,
        rq = rq
    )

    @GetMapping("geo")
    fun geo(
        @RequestHeader("Currency", required = false)
        currency: String? = null,
    ) = estateProcessor.geo(currency = currency)

    @GetMapping("{id}/units")
    fun findUnits(
        @RequestHeader("Currency", required = false)
        currency: String? = null,
        @PathVariable id: UUID,
        @RequestParam(required = false) orderBy: String? = null,
        @RequestParam(required = false) rooms: Set<String>? = null,
        @RequestParam(required = false) minPrice: Double? = null,
        @RequestParam(required = false) maxPrice: Double? = null,
        @RequestParam(required = false) minSize: Double? = null,
        @RequestParam(required = false) maxSize: Double? = null,
        @RequestParam(required = false) minPriceSq: Double? = null,
        @RequestParam(required = false) maxPriceSq: Double? = null,
    ): UnitsRs = estateProcessor.findUnits(
        currency = currency,
        id = id,
        orderBy = orderBy,
        rooms = rooms,
        minPrice = minPrice,
        maxPrice = maxPrice,
        minSize = minSize,
        maxSize = maxSize,
        minPriceSq = minPriceSq,
        maxPriceSq = maxPriceSq
    )

    @GetMapping("main/info")
    fun getMainInfo(
        @RequestParam userId: UUID
    ): MainInfoDto = estateProcessor.getMainInfo(userId)

    @GetMapping("generate/xml", produces = [MediaType.APPLICATION_XML_VALUE])
    fun generateXml() = estateProcessor.prepareXml()

    @GetMapping("generate/json")
    fun generateJson() = estateProcessor.prepareJson()

    @GetMapping("generate/json/unit")
    fun generateJsonUnit() = estateProcessor.prepareJsonUnit()
}

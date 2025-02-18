package ru.nemodev.insightestate.api.client.v1.controller

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
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
import java.util.*

@RestController
@RequestMapping("/v1/estate", produces = [MediaType.APPLICATION_JSON_VALUE])
class EstateController (
    private val service: EstateService
) {

    @GetMapping
    fun findAll(

        // TODO описать примеры параметров + сделать валидацию
        @RequestParam(name = "types", required = false)
        types: Set<EstateType>?,

        /**
         * Дата сдачи объекта
         * 2025
         * 2026
         * 2027
         * 2028
         */
        @RequestParam(name = "buildEndYears", required = false)
        buildEndYears: Set<Int>?,

        /**
         * Количество комнат
         * 0 - Студия
         * 1 - 1 cпальня
         * 2 - 2 спальни
         * 3 - 3 спальни
         * 4 - 4+ спальни
         */
        @RequestParam(name = "rooms", required = false)
        rooms: Set<String>?,

        /**
         * Стоимость
         * 1 - до $100 000
         * 2 - $100 000 — $200 000
         * 3 - $200 000 — $500 000
         * 4 - $500 000 — $1 000 000
         * 5 - от $1 000 000
         */
        @RequestParam(name = "price", required = false)
        price: String?,

        /**
         * Комфорт и инвестиционный потенциал
         * 1 Самые безопасные для инвестиций
         * 2 Наибольшая доходность
         * 3 Самые удобные локации
         * 4 Самые комфортные для жизни
         */
        @RequestParam(name = "grades", required = false)
        grades: Set<String>?,

        @RequestParam(name = "pageNumber", required = false)
        @Valid
        @Min(0, message = "Минимальное значение 0")
        pageNumber: Int? = 0,

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
        pageable = PageRequest.of(
            pageNumber ?: 0,
            pageSize ?: 25
        )
    )

    @GetMapping("/{id}")
    fun findById(
        @PathVariable
        id: UUID,
    ): EstateDetailDtoRs = service.findById(id)
}

package ru.nemodev.insightestate.api.client.v1.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ru.nemodev.insightestate.api.client.v1.dto.EstateInfoDto
import ru.nemodev.insightestate.api.client.v1.dto.EstateListDto
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.insightestate.service.EstateService
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
import java.util.*

@RestController
@RequestMapping("estate", produces = [MediaType.APPLICATION_JSON_VALUE])
class EstateController (
    private val service: EstateService
) {
    /**
     * АПИ для получения списка объектов недвижимости с пагинацией и фильтрами
     */
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findAll(
        /**
         * Комфорт и инвестиционный потенциал
         * 0 Наибольшая доходность
         * 1 Самые безопасные для инвестиций
         * 2 Самые комфортные для жизни
         * 3 Самые удобные локации
         */
        @RequestParam(name = "potential", required = false)
        potential: List<Int>? = null,

        /**
         * Стоимость
         * 100000 - до $100 000
         * 200000 - $100 000 — $200 000
         * 500000 - $200 000 — $500 000
         * 1000000 - $500 000 — $1 000 000
         * 1000001 - от $1 000 000
         */
        @RequestParam(name = "price", required = false)
        price: Long? = null,

        /**
         * Количество спален
         * 0 - Студия
         * 1 - 1 cпальня
         * 2 - 2 спальни
         * 3 - 3 спальни
         * 4 - 4+ спальни
         */
        @RequestParam(name = "beds", required = false)
        beds: List<String>? = null,

        /**
         * Дата сдачи объекта
         * 2025
         * 2026
         * 2027
         * 2028
         */
        @RequestParam(name = "year", required = false)
        year: List<Int>? = null,

        /**
         * Тип объекта
         * APARTMENT Квартира
         * VILLA Вилла
         */
        @RequestParam(name = "type", required = false)
        type: EstateType? = null,

        @RequestParam(name = "pageNumber", required = false)
        @Valid
        @Min(0, message = "Минимальное значение 0")
        pageNumber: Int? = null,

        @RequestParam(name = "pageSize", required = false)
        @Valid
        @Min(1, message = "Минимальное значение 1")
        @Max(100, message = "Максимальное значение 100")
        pageSize: Int? = null
    ): PageDtoRs<EstateListDto> = service.findAll(
        potential = potential,
        price = price,
        beds = beds,
        year = year,
        type = type,
        pageable = PageRequest.of(
            pageNumber ?: 0,
            pageSize ?: 25
        )
    )

    /**
     * АПИ для получения детальной информации по объекту недвижимости
     */
    @GetMapping("{id}")
    fun findById(
        @PathVariable
        id: UUID,
    ): EstateInfoDto = service.findById(id)
}

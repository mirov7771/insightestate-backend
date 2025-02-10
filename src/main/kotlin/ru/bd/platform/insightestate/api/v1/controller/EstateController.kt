package ru.bd.platform.insightestate.api.v1.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.bd.platform.insightestate.api.v1.dto.EstateListDto
import ru.bd.platform.insightestate.entity.EstateType
import ru.bd.platform.insightestate.service.EstateService
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs

@RestController
@RequestMapping("estate", produces = [MediaType.APPLICATION_JSON_VALUE])
class EstateController (
    private val service: EstateService
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findAll(
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
        type = type,
        pageable = PageRequest.of(
            pageNumber ?: 0,
            pageSize ?: 25
        )
    )
}

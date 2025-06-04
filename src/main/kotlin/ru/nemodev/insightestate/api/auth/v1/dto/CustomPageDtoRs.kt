package ru.nemodev.insightestate.api.auth.v1.dto

import io.swagger.v3.oas.annotations.media.Schema
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDtoRs
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs

data class CustomPageDtoRs(
    val totalPages: Int,
    @Schema(description = "Набор элементов на текущей странице")
    val items: List<EstateDtoRs>,

    @Schema(description = "Номер страницы", example = "0", minimum = "0", maximum = "999999999")
    val pageNumber: Int = 0,

    @Schema(description = "Кол-во элементов на странице", example = "25", minimum = "0", maximum = "999999999")
    val pageSize: Int = items.size,

    @Schema(description = "Признак наличия оставшихся страниц")
    val hasMore: Boolean = items.size >= pageSize,

    val totalCount: Int = 0
) {

    companion object {
        fun <T> empty() = PageDtoRs<T>(emptyList(), 0, 0)
    }
}

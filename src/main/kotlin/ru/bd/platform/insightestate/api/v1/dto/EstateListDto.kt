package ru.bd.platform.insightestate.api.v1.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EstateListDto (
    val id: UUID,
    val rate: String,
    val name: String,
    val price: Long,
    val profitAmount: Long? = null,
    val profitTerm: Int? = null,
    val images: List<String>,
    val deliveryDate: LocalDate,
    val level: String,
    val beach: String,
)

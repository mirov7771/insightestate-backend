package ru.bd.platform.insightestate.api.v1.dto

import java.time.LocalDate
import java.util.UUID

data class EstateListDto (
    val id: UUID,
    val rate: String,
    val name: String,
    val price: Long,
    val profitAmount: Long,
    val profitTerm: Int,
    val images: List<String>,
    val deliveryDate: LocalDate,
    val level: String,
    val beach: String,
)

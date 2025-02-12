package ru.nemodev.insightestate.api.client.v1.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EstateListDto (
    val id: UUID,
    val rate: String,
    val name: String,
    val price: String? = null,
    val profitAmount: Long? = null,
    val profitTerm: String? = null,
    val images: List<String>,
    val deliveryDate: String? = null,
    val level: String,
    val beach: String,
)

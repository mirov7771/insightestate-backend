package ru.nemodev.insightestate.api.client.v1.dto.tariff

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TariffRs (
    val main: List<TariffDto>,
    val extra: List<TariffDto>?
)

data class TariffDto (
    val id: UUID,
    val title: String,
    val description: List<String>,
    val price: BigDecimal,
)

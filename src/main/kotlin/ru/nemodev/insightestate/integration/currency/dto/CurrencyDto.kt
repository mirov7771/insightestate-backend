package ru.nemodev.insightestate.integration.currency.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class CurrencyDto (
    val rates: CurrencyRate? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CurrencyRate (
    @JsonProperty("RUB")
    val rub: BigDecimal? = null,
    @JsonProperty("THB")
    val thb: BigDecimal? = null,
    @JsonProperty("USD")
    val usd: BigDecimal? = null,
    @JsonProperty("AUD")
    val aud: BigDecimal? = null
)

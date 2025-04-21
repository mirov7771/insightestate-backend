package ru.nemodev.insightestate.integration.ai.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class PromptRq (
    val model: String = "qwen2.5-coder:3b",
    val prompt: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PromptRs (
    val response: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResultRs (
    val result: ResultDto
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResultDto (
    val type: String? = null,
    val rooms: String? = null,
    val gym: String? = null,
    val beach: String? = null,
    val shop: String? = null,
    val childRoom: String? = null,
    val parking: String? = null,
    val beachTravelTimesCar: String? = null,
    val beachTravelTimesWalk: String? = null,
    val beachTravelTimes: String? = null,
    val airportTravelTimes: String? = null,
    val priceFrom: String? = null,
    val priceTo: String? = null,
    val currency: String? = null,
    val buildEndYears: String? = null,
    val city: String? = null,
    val isUk: String? = null,
    val rating: String? = null,
    val roi: String? = null,
    val mallTravelTimes: String? = null,
)

fun ResultDto.isEmpty() = this.type == null &&
        this.beach == null &&
        this.city == null &&
        this.priceFrom == null &&
        this.priceTo == null &&
        this.beachTravelTimesWalk == null &&
        this.beachTravelTimesCar == null &&
        this.buildEndYears == null &&
        this.airportTravelTimes == null &&
        this.isUk == null &&
        this.rooms == null &&
        this.rating == null &&
        this.roi == null &&
        this.mallTravelTimes == null

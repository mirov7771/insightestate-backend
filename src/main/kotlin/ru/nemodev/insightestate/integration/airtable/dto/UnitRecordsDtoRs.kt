package ru.nemodev.insightestate.integration.airtable.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime


@JsonIgnoreProperties(ignoreUnknown = true)
data class UnitRecordsDtoRs(
    val records: List<AirtableRecordDto>,
    val offset: String?
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AirtableRecordDto(
        val id: String,

        @JsonProperty("createdTime")
        val createdTime: String,

        val fields: AirtableProjectFieldsDto
    ) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class AirtableProjectFieldsDto(

            @JsonProperty("Unit ID")
            val unitId: String,

            @JsonProperty("Проект")
            val projectIds: List<String>?,

            @JsonProperty("Корпус")
            val corpus: String?,

            @JsonProperty("Номер юнита")
            val unitNumber: String?,

            @JsonProperty("Этаж")
            val floor: String?,

            @JsonProperty("Количество комнат")
            val rooms: String?,

            @JsonProperty("Площадь")
            val square: String?,

            @JsonProperty("Цена за м2")
            val pricePerM2: String?,

            @JsonProperty("Стоимость")
            val totalPrice: String?,

            @JsonProperty("Код планировки")
            val layoutCode: String?,

            @JsonProperty("updatedAt")
            val updatedAt: LocalDateTime
        )
    }
}

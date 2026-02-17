package ru.nemodev.insightestate.integration.airtable.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime


@JsonIgnoreProperties(ignoreUnknown = true)
data class EstateRecordsDtoRs(
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

            @JsonProperty("Project ID")
            val projectId: String,

            @JsonProperty("Проект")
            val projectName: String,

            @JsonProperty("Выкуплена земля")
            val landPurchased: String?,

            @JsonProperty("Наличие EIA")
            val hasEia: String?,

            @JsonProperty("Developer")
            val developer: String,

            @JsonProperty("Количество проектов, шт.")
            val totalProjects: Int,

            @JsonProperty("Количество проектов на этапе строительства, шт.")
            val countProjectsInConstruction: Int,

            @JsonProperty("Количество проектов сданных, шт.")
            val countCompletedProjects: Int,

            @JsonProperty("Город")
            val city: String,

            @JsonProperty("Телефон застройщика")
            val developerPhone: String?,

            @JsonProperty("Электронная почта застройщика")
            val developerEmail: String?,

            @JsonProperty("Статус проекта")
            val projectStatus: String,

            @JsonProperty("Окончание сторительства, мес. год.")
            val buildEndDate: LocalDate?,

            @JsonProperty("Общее количество юнитов, шт.")
            val totalUnits: Int,

            @JsonProperty("Количество проданных юнитов, шт.")
            val sailedUnits: Int?,

            @JsonProperty("Остаток юнитов, шт.")
            val availableUnits: Int?,

            @JsonProperty("Локация")
            val location: String,

            @JsonProperty("Район")
            val district: String,

            @JsonProperty("Пляж")
            val beach: String?,

            @JsonProperty("Время пути до моря пешком, мин.")
            val walkToBeachMinutes: Int,

            @JsonProperty("Время пути до моря на машине, мин.")
            val driveToBeachMinutes: Int,

            @JsonProperty("Время пути до аэропорта, мин.")
            val airportMinutes: Int,

            @JsonProperty("Время до ближайшего крупного ТЦ на машине, мин.")
            val mallDriveMinutes: Int,

            @JsonProperty("Время до ближайшего крупного ТЦ на пешком, мин.")
            val mallWalkMinutes: Int,

            @JsonProperty("Класс")
            val estateLevel: String?,

            @JsonProperty("Тип объекта")
            val propertyType: String,

            @JsonProperty("Этажность")
            val floors: Int?,

            @JsonProperty("Наличие УК")
            val hasManagementCompany: String?,

            @JsonProperty("Спорт")
            val sport: String?,

            @JsonProperty("Для детей")
            val forChildren: String?,

            @JsonProperty("Магазины")
            val shops: String?,

            @JsonProperty("Развлекательные")
            val entertainment: String?,

            @JsonProperty("Коворкинг")
            val coworking: String?,

            @JsonProperty("Pet-Friendly")
            val petFriendly: String?,

            @JsonProperty("Мебельный пакет")
            val furniturePackage: String?,

            @JsonProperty("Гарантированный доход от застройщика")
            val guaranteedDeveloperIncome: String?,

            @JsonProperty("Показатель доходности гарантированный доход, %")
            val guaranteedIncomeYieldPercent: BigDecimal?,

            @JsonProperty("Краткое описание проектов")
            val descriptionRu: String?,

            @JsonProperty("Eng описание проектов")
            val descriptionEn: String?,

            @JsonProperty("Лидеры продаж")
            val salesLeaders: String?,

            @JsonProperty("Выбор платформы")
            val platformSelection: String?,

            @JsonProperty("Выбор брокеров")
            val brokerSelection: String?,

            @JsonProperty("Sold Out")
            val soldOut: String,

            @JsonProperty("lat")
            val latitude: String?,

            @JsonProperty("lon")
            val longitude: String?,

            @JsonProperty("updatedAt")
            val updatedAt: LocalDateTime,

            @JsonProperty("Презентация проекта")
            val presentation: String?,

            @JsonProperty("Паркинг")
            val parkingSize: String?,

            @JsonProperty("График платежей")
            val paymentPlan: String?,

            @JsonProperty("Средняя площадь")
            val size: Long?,

            @JsonProperty("Минимальная стоимость, Виллы")
            val minPriceVillas: String?,

            @JsonProperty("Максимальная стоимость, Виллы")
            val maxPriceVillas: String?,

            // планировки комнат
            @JsonProperty("Минимальная цена за м2, Studio")
            val minPricePerM2Studio: String?,

            @JsonProperty("Максимальная цена за м2, Studio")
            val maxPricePerM2Studio: String?,

            @JsonProperty("Средняя цена за м2, Studio")
            val avgPricePerM2Studio: String?,

            @JsonProperty("Минимальная стоимость, Studio")
            val minTotalPriceStudio: String?,

            @JsonProperty("Максимальная стоимость, Studio")
            val maxTotalPriceStudio: String?,

            @JsonProperty("Средняя стоимость, Studio")
            val avgTotalPriceStudio: String?,

            @JsonProperty("Минимальная площадь Studio")
            val minAreaStudio: String?,

            @JsonProperty("Максимальная площадь Studio")
            val maxAreaStudio: String?,


            @JsonProperty("Минимальная цена за м2, 1Br")
            val minPricePerM21Br: String?,

            @JsonProperty("Максимальная цена за м2, 1Br")
            val maxPricePerM21Br: String?,

            @JsonProperty("Средняя цена за м2, 1Br")
            val avgPricePerM21Br: String?,

            @JsonProperty("Минимальная стоимость, 1Br")
            val minTotalPrice1Br: String?,

            @JsonProperty("Максимальная стоимость, 1Br")
            val maxTotalPrice1Br: String?,

            @JsonProperty("Средняя стоимость, 1Br")
            val avgTotalPrice1Br: String?,

            @JsonProperty("Минимальная площадь 1 Br")
            val minArea1Br: String?,

            @JsonProperty("Максимальная площадь 1 Br")
            val maxArea1Br: String?,


            @JsonProperty("Минимальная цена за м2, 2Br")
            val minPricePerM22Br: String?,

            @JsonProperty("Максимальная цена за м2, 2Br")
            val maxPricePerM22Br: String?,

            @JsonProperty("Средняя цена за м2, 2Br")
            val avgPricePerM22Br: String?,

            @JsonProperty("Минимальная стоимость, 2Br")
            val minTotalPrice2Br: String?,

            @JsonProperty("Максимальная стоимость, 2Br")
            val maxTotalPrice2Br: String?,

            @JsonProperty("Средняя стоимость, 2Br")
            val avgTotalPrice2Br: String?,

            @JsonProperty("Минимальная площадь 2 Br")
            val minArea2Br: String?,

            @JsonProperty("Максимальная площадь 2 Br")
            val maxArea2Br: String?,


            @JsonProperty("Минимальная цена за м2, 3Br")
            val minPricePerM23Br: String?,

            @JsonProperty("Максимальная цена за м2, 3Br")
            val maxPricePerM23Br: String?,

            @JsonProperty("Средняя цена за м2, 3Br")
            val avgPricePerM23Br: String?,

            @JsonProperty("Минимальная стоимость, 3Br")
            val minTotalPrice3Br: String?,

            @JsonProperty("Максимальная стоимость, 3Br")
            val maxTotalPrice3Br: String?,

            @JsonProperty("Средняя стоимость, 3Br")
            val avgTotalPrice3Br: String?,

            @JsonProperty("Минимальная площадь 3 Br")
            val minArea3Br: String?,

            @JsonProperty("Максимальная площадь 3 Br")
            val maxArea3Br: String?,


            @JsonProperty("Минимальная цена за м2, 4Br")
            val minPricePerM24Br: String?,

            @JsonProperty("Максимальная цена за м2, 4Br")
            val maxPricePerM24Br: String?,

            @JsonProperty("Средняя цена за м2, 4Br")
            val avgPricePerM24Br: String?,

            @JsonProperty("Минимальная стоимость, 4Br")
            val minTotalPrice4Br: String?,

            @JsonProperty("Максимальная стоимость, 4Br")
            val maxTotalPrice4Br: String?,

            @JsonProperty("Средняя стоимость, 4Br")
            val avgTotalPrice4Br: String?,

            @JsonProperty("Минимальная площадь 4 Br")
            val minArea4Br: String?,

            @JsonProperty("Максимальная площадь 4 Br")
            val maxArea4Br: String?,


            @JsonProperty("Минимальная цена за м2, 5Br")
            val minPricePerM25Br: String?,

            @JsonProperty("Максимальная цена за м2, 5Br")
            val maxPricePerM25Br: String?,

            @JsonProperty("Средняя цена за м2, 5Br")
            val avgPricePerM25Br: String?,

            @JsonProperty("Минимальная стоимость, 5Br")
            val minTotalPrice5Br: String?,

            @JsonProperty("Максимальная стоимость, 5Br")
            val maxTotalPrice5Br: String?,

            @JsonProperty("Средняя стоимость, 5Br")
            val avgTotalPrice5Br: String?,

            @JsonProperty("Минимальная площадь 5 Br")
            val minArea5Br: String?,

            @JsonProperty("Максимальная площадь 5 Br")
            val maxArea5Br: String?,


            @JsonProperty("Минимальная цена за м2, Вилла")
            val minPricePerM2Villa: String?,

            @JsonProperty("Максимальная цена за м2, Вилла")
            val maxPricePerM2Villa: String?,

            @JsonProperty("Средняя цена за м2, Вилла")
            val avgPricePerM2Villa: String?,

            @JsonProperty("Минимальная стоимость Villa 2 BR")
            val minTotalPriceVilla2Br: String?,

            @JsonProperty("Максимальная стоимость Villa 2 BR")
            val maxTotalPriceVilla2Br: String?,

            @JsonProperty("Средняя стоимость, Виллы")
            val avgTotalPriceVilla: String?,

            @JsonProperty("Минимальная площадь Villa 2 BR")
            val minAreaVilla2Br: String?,

            @JsonProperty("Максимальная площадь Villa 2 BR")
            val maxAreaVilla2Br: String?,


            @JsonProperty("Минимальная стоимость Villa 3 BR")
            val minTotalPriceVilla3Br: String?,

            @JsonProperty("Максимальная стоимость Villa 3 BR")
            val maxTotalPriceVilla3Br: String?,

            @JsonProperty("Минимальная площадь Villa 3 BR")
            val minAreaVilla3Br: String?,

            @JsonProperty("Максимальная площадь Villa 3 BR")
            val maxAreaVilla3Br: String?,


            @JsonProperty("Минимальная стоимость Villa 4 BR")
            val minTotalPriceVilla4Br: String?,

            @JsonProperty("Максимальная стоимость Villa 4 BR")
            val maxTotalPriceVilla4Br: String?,

            @JsonProperty("Минимальная площадь Villa 4 BR")
            val minAreaVilla4Br: String?,

            @JsonProperty("Максимальная площадь Villa 4 BR")
            val maxAreaVilla4Br: String?,


            @JsonProperty("Минимальная стоимость Villa 5 BR")
            val minTotalPriceVilla5Br: String?,

            @JsonProperty("Максимальная стоимость Villa 5 BR")
            val maxTotalPriceVilla5Br: String?,

            @JsonProperty("Минимальная площадь Villa 5 BR")
            val minAreaVilla5Br: String?,

            @JsonProperty("Максимальная площадь Villa 5 BR")
            val maxAreaVilla5Br: String?,

            // Поля для Грузии
            @JsonProperty("Парковка цена")
            val parkingPrice: BigDecimal?,

            @JsonProperty("Парковка — Способ покупки")
            val parkingPurchaseMethod: String?,

            @JsonProperty("Название УК")
            val managementCompanyName: String?,

            @JsonProperty("Стоимость обслуживания")
            val serviceCost: BigDecimal?,

            @JsonProperty("Условия проживания")
            val livingConditions: String?,

            @JsonProperty("Условия бронирования")
            val bookingConditions: String?,

            @JsonProperty("Метод распределения доходности")
            val incomeDistributionMethod: String?,
        )
    }
}

package ru.nemodev.insightestate.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.annotation.StoreJson
import ru.nemodev.platform.core.db.entity.AbstractEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Table("estate")
class EstateEntity (
    id: UUID? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column("estate_detail")
    var estateDetail: EstateDetail
) : AbstractEntity<UUID>(id, createdAt, updatedAt)

/**
 * Описание полей согласно таблице
 * https://docs.google.com/spreadsheets/d/1Oa2NZEsBeO6BTh5f9bWm_0bR0YvF_guyGr6t2lGBcms/edit?gid=0#gid=0
 */
@StoreJson
data class EstateDetail (
    val projectId: String,                      // id проекта
    val name: String,                           // название проекта
    val shortDescriptionRu: String? = null,     // краткое описание ru
    val shortDescriptionEn: String? = null,     // краткое описание en

    val landPurchased: Boolean,                 // земля выкуплена
    val eiaEnabled: Boolean,                    // наличие EIA
    val developer: EstateDeveloper,             // застройщик
    val grade: EstateGrade,                     // оценка проекта
    val projectCount: ProjectCount,             // количество проектов
    val status: EstateStatus,                   // статус проекта
    val saleStartDate: LocalDate? = null,       // дата начала продаж // TODO в таблице нужно поменять формат на дату как у даты окончания строительства
    val buildEndDate: LocalDate? = null,        // дата окончания строительства
    val unitCount: UnitCount,                   // количество юнитов

    var type: EstateType,                       // тип проекта
    val level: EstateLevelType,                 // класс проекта
    val product: EstateProductType,             // тип продукта
    val profitability: EstateProfitability,     // доходность

    val location: EstateLocation,               // локация
    val infrastructure: EstateInfrastructure,   // инфраструктура
    val options: EstateOptions,                 // опции

    val price: MinMaxAvgParam,                  // стоимость
    val ceilingHeight: BigDecimal? = null,      // высота потолка
    val floors: Int? = null,                    // этажность
    val roomLayouts: RoomLayouts,               // планировки комнат

    var facilityImages: MutableList<String>? = null,   // фото объекта
    var exteriorImages: MutableList<String>? = null,   // фото экстерьера
    var interiorImages: MutableList<String>? = null,   // фото интерьера
)

// Оценка проекта
data class EstateGrade(
    val main: BigDecimal,                   // итоговая оценка
    val investmentSecurity: BigDecimal,     // безопасность вложений
    val investmentPotential: BigDecimal,    // инвестиционный потенциал
    val projectLocation: BigDecimal,        // расположение проекта
    val comfortOfLife: BigDecimal,          // комфорт жизни
)

// Застройщик
data class EstateDeveloper(
    val name: String,                   // название
    val country: String? = null,        // страна
    val yearOfFoundation: Int? = null,  // год основания
)

// Число проектов
data class ProjectCount(
    val total: Int,                             // количество всего
    val build: Int,                             // количество на этапе строительства
    val finished: Int,                          // количество завершенных
    val deviationFromDeadline: Int? = null,     // количество просроченных
)

// Число юнитов
data class UnitCount(
    val total: Int,                                 // всего
    val sailed: Int? = null,                        // продано
    val available: Int? = null                      // доступно
)

// Локация проекта
data class EstateLocation(
    val name: String,                               // локация
    val district: String,                           // район
    val beach: String,                              // пляж
    val mapUrl: String,                             // ссылка на карту
)

// Инфраструктура
data class EstateInfrastructure(
    val beachTime: TravelTime,                      // время в пути до пляжа
    val airportTime: TravelTime,                    // время в пути до аэропорта
    val mallTime: TravelTime,                       // время в пути до тц
    val schoolRadius: BigDecimal,                   // школа в радиусе в км
    val nurserySchoolRadius: BigDecimal? = null,    // детский сад в радиусе в км
)

// Время в пути в минутах
data class TravelTime(
    val walk: Int?,                                 // пешком
    val car: Int,                                   // на машине
)

// Доходность
data class EstateProfitability(
    val roi: BigDecimal,
    val roiSummary: BigDecimal,
    val irr: BigDecimal,
    val capRateFirstYear: BigDecimal,
)

// Опции
data class EstateOptions(
    val parkingSize: Int? = null,       // паркинг
    val gym: Boolean,                   // спортзал
    val childRoom: Boolean,             // для детей
    val shop: Boolean,                  // магазины
    val entertainment: Boolean,         // развлекательные
    val coworking: Boolean = false,     // co-working
)

// Планировки
data class RoomLayouts(
    val studio: RoomParams? = null,
    val one: RoomParams? = null,
    val two: RoomParams? = null,
    val three: RoomParams? = null,
    val four: RoomParams? = null,
    val five: RoomParams? = null,
    val villaTwo: RoomParams? = null,
    val villaThree: RoomParams? = null,
    val villaFour: RoomParams? = null,
    val villaFive: RoomParams? = null,
)

// Параметры планировки
data class RoomParams(
    val pricePerMeter: MinMaxAvgParam,
    val price: MinMaxAvgParam,
    val square: MinMaxAvgParam,
)

data class MinMaxAvgParam (
    val min: BigDecimal,
    val max: BigDecimal,
    val avg: BigDecimal? = null,
)

enum class EstateLevelType(val csv: String) {
    COMFORT("Комфорт"),
    LUX("Люкс"),
    PREMIUM("Премиум"),
    UNKNOWN("Не указан"),
}

enum class EstateProductType {
    INVESTMENT,
    RESIDENCE,
    UNKNOWN
}

enum class EstateReadyStatus {
    DONE,
    SOLD_OUT
}

enum class EstateType(val csv: String) {
    VILLA("villa"),
    APARTMENT("kvartira")
}

enum class EstateStatus {
    BUILD,
    FINISHED,
    UNKNOWN
}



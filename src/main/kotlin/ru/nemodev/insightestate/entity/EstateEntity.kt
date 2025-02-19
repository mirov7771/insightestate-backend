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
    val estateDetail: EstateDetail
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
    val readyStatus: EstateReadyStatus,         // статус готовности TODO вроде это поле не нужно вообще т.к в таблице как фильтр объектов которые доступны на сайте
    val priority: Int? = null,                  // приоритет TODO вроде это поле не нужно вообще т.к в таблице используется как приоритет для заполнения полей?
    val developer: EstateDeveloper,             // застройщик
    val grade: EstateGrade,                     // оценка проекта
    val projectCount: ProjectCount,             // количество проектов
    val status: EstateStatus,                   // статус проекта
    val saleDate: LocalDate? = null,            // дата начала продаж // TODO вроде поле нигде не требуется - в таблице нужно поменять формат на дату как у даты окончания строительства
    val buildEndDate: LocalDate? = null,        // дата окончания строительства
    val unitCount: UnitCount,                   // количество юнитов
    val constructionSchedule: String? = null,   // график строительства // TODO вроде поле не нужно и везде пустое?

    var type: EstateType,                       // тип проекта
    val level: EstateLevelType,                 // класс проекта
    val product: EstateProductType,             // тип продукта
    val profitability: EstateProfitability,     // доходность

    val location: EstateLocation,               // локация
    val infrastructure: EstateInfrastructure,   // инфраструктура
    val options: EstateOptions,                 // опции

    val price: MinMaxAvgParam,                  // стоимость
    val ceilingHeight: BigDecimal? = null,      // высота потолка
    val floors: Int? = null,                    // этажей - TODO где брать эту инфу?
    val roomLayouts: RoomLayouts,               // планировки комнат

    val projectImage: String? = null,           // фото проекта
    val images: List<String>? = null,           // остальные фото
)

// Оценка проекта
data class EstateGrade(
    val final: BigDecimal,                  // итоговая оценка
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
    val available: Int                              // доступно
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
    val coWorking: Boolean,             // co-working
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

enum class EstateLevelType {
    COMFORT,
    LUX,
    PREMIUM,
    UNKNOWN,
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

enum class EstateType {
    VILLA,
    APARTMENT
}

enum class EstateStatus {
    BUILD,
    FINISHED,
    UNKNOWN
}



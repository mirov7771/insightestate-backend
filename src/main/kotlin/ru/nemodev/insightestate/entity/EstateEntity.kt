package ru.nemodev.insightestate.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.annotation.StoreJson
import ru.nemodev.platform.core.db.entity.AbstractEntity
import ru.nemodev.platform.core.extensions.isNotNullOrEmpty
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
) : AbstractEntity<UUID>(id, createdAt, updatedAt) {

    fun isCanShow(): Boolean {
        return estateDetail.facilityImages.isNotNullOrEmpty()
                || estateDetail.exteriorImages.isNotNullOrEmpty()
                || estateDetail.interiorImages.isNotNullOrEmpty()
    }
}

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

    val landPurchased: Boolean?,                 // земля выкуплена
    val eiaEnabled: Boolean?,                    // наличие EIA
    val developer: EstateDeveloper,             // застройщик
    val grade: EstateGrade,                     // оценка проекта (больше не используются)
    val projectCount: ProjectCount,             // количество проектов
    val status: EstateStatus,                   // статус проекта
    var canShow: Boolean = true,                // можно ли показывать объект
    val saleStartDate: LocalDate? = null,       // дата начала продаж // TODO в таблице нужно поменять формат на дату как у даты окончания строительства
    val buildEndDate: LocalDate? = null,        // дата окончания строительства
    val unitCount: UnitCount,                   // количество юнитов
    val soldOut: Boolean? = null,               // Sold Out

    var type: EstateType,                       // тип проекта
    val level: EstateLevelType,                 // класс проекта
    val product: EstateProductType? = null,     // тип продукта (не используется)
    val profitability: EstateProfitability,     // доходность

    val location: EstateLocation,               // локация
    val infrastructure: EstateInfrastructure,   // инфраструктура
    val options: EstateOptions,                 // опции
    val managementCompany: ManagementCompany = ManagementCompany(),   // управляющая компания

    val price: MinMaxAvgParam,                  // стоимость
    val ceilingHeight: BigDecimal? = null,      // высота потолка
    val floors: Int? = null,                    // этажность
    val roomLayouts: RoomLayouts,               // планировки комнат

    var facilityImages: MutableList<String>? = null,   // фото объекта
    var exteriorImages: MutableList<String>? = null,   // фото экстерьера
    var interiorImages: MutableList<String>? = null,   // фото интерьера

    val paymentPlan: String? = null,
    var likesCount: Long? = null,
    var toolTip1: String? = null,      // лидеры продаж
    var toolTip2: String? = null,      // выбор платформы
    var toolTip3: String? = null,      // выбор брокеров
    var collectionCount: Int? = null,

    var units: List<UnitEntity>? = null,

    val lat: String? = null,
    val lon: String? = null,
    val size: Long? = null,                 // Средняя площадь

    val furniture: String? = null,          // Мебельный пакет
    var rusPresentation: Boolean = false,
    var engPresentation: Boolean = false,

    // Поля для Грузии
    val parkingPrice: BigDecimal? = null,                          // Парковка цена
    val parkingPurchaseMethod: ParkingPurchaseMethod? = null,      // Парковка — Способ покупки
    val managementCompanyName: String? = null,                     // Название УК
    val serviceCost: BigDecimal? = null,                           // Стоимость обслуживания
    val livingConditions: String? = null,                          // Условия проживания
    val bookingConditions: String? = null,                         // Условия бронирования
    val incomeDistributionMethod: String? = null,                  // Метод распределения доходности
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
    val phone: String? = null,
    val email: String? = null,
    var presentation: String? = null,
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
    val beach: String?,                             // пляж
    val mapUrl: String,                             // ссылка на карту
    val city: String = "Phuket",                    // Город
)

// Инфраструктура
data class EstateInfrastructure(
    val beachTime: TravelTime,                      // время в пути до пляжа
    val airportTime: TravelTime,                    // время в пути до аэропорта
    val mallTime: TravelTime,                       // время в пути до тц
    val school: School? = School()                  // школа (не используется)
) {
    data class School(
        val radius: BigDecimal = BigDecimal.ZERO,
        val name: String? = null
    )
}

// Управляющая компания
data class ManagementCompany(
    val enabled: Boolean = false
)

// Время в пути в минутах
data class TravelTime(
    val walk: Int?,                                 // пешком
    val car: Int,                                   // на машине
)

// Доходность
data class EstateProfitability(
    val roi: BigDecimal,                            // не используется
    val roiSummary: BigDecimal,                     // не используется
    val irr: BigDecimal,                            // не используется
    val capRateFirstYear: BigDecimal,               // не используется
    val guarantee: BigDecimal? = null,               // Показатель доходности гарантированный доход, %
    val guaranteedDeveloperIncome: Boolean? = null   // Гарантированный доход от застройщика
)

// Опции
data class EstateOptions(
    val parkingSize: Int? = null,       // паркинг
    val gym: Boolean,                   // спортзал
    val childRoom: Boolean,             // для детей
    val shop: Boolean,                  // магазины
    val entertainment: Boolean,         // развлекательные
    val coworking: Boolean,             // co-working
    val petFriendly: Boolean = false,   // животные приветствуются
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
    val pricePerMeter: MinMaxAvgParam? = null,
    val price: MinMaxAvgParam? = null,
    val square: MinMaxAvgParam? = null,
)

data class MinMaxAvgParam (
    var min: BigDecimal,
    var max: BigDecimal,
    var avg: BigDecimal? = null,
)

enum class EstateLevelType {
    COMFORT,
    LUX,
    PREMIUM,
    UNKNOWN
}

enum class EstateProductType {
    INVESTMENT,
    RESIDENCE,
    UNKNOWN
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

enum class ParkingPurchaseMethod {
    WITHOUT_APARTMENT,
    ONLY_APARTMENT
}


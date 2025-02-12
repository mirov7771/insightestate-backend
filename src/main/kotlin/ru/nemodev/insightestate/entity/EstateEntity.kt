package ru.nemodev.insightestate.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.annotation.StoreJson
import ru.nemodev.platform.core.db.entity.AbstractEntity
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
    /**
     * Project Id
     */
    val projectId: String,
    /**
     * Проект
     */
    val name: String,
    /**
     * Выкуплена земля
     */
    val landSold: Boolean,
    /**
     * Наличие EIA
     */
    val eia: Boolean,
    /**
     * Готовность
     */
    val readiness: String,
    /**
     * Приоритет
     */
    val priority: Int = 0,
    /**
     * Developer
     */
    val developer: String,
    /**
     * Наша итоговая оценка
     */
    val rate: String,
    /**
     * Безопасность вложений
     */
    val attachmentSecurity: String,
    /**
     * Инвестиционный потенциал
     */
    val investmentPotential: String,
    /**
     * Расположение проекта
     */
    val locationOfTheObject: String,
    /**
     * Комфорт жизни
     */
    val comfortOfLife: String,
    /**
     * Окончание строительства, мес. год.
     */
    val deliveryDate: LocalDate,
    /**
     * Локация
     */
    val location: String,
    /**
     * Район
     */
    val district: String,
    /**
     * Пляж
     */
    val beachName: String,
    /**
     * Время пути до моря пешком, мин.
     */
    val beach: String,
    /**
     * Время пути до моря на машине, мин.
     */
    val beachCar: String,
    /**
     * Время пути до аэропорта, мин.
     */
    val airport: String,
    /**
     * Время до ближайшего крупного ТЦ на машине, мин.
     */
    val mall: String,
    /**
     * Время до ближайшего крупного ТЦ на пешком, мин.
     */
    val mallWalk: String,
    /**
     * Класс
     */
    val level: String,

    /**
     * Паркинг
     */
    val parking: String? = null,
    /**
     * IRR
     */
    val irr: String? = null,
    /**
     * 10-year ROI
     */
    val roi: String? = null,

    /**
     * Ссылка на карту
     */
    val geoPosition: String,
    /**
     * Суммарный ROI
     */
    val summaryRoi: String? = null,
    /**
     * Краткое описание проектов для слайдов
     */
    val shortDescription: String? = null,
    /**
     * Eng описание проектов для слайдов (перевод)
     */
    val engDescription: String? = null,
    /**
     * Минимальная - Максимальная площадь
     */
    val square: Square,
    /**
     * Общее количество юнитов, шт.
     */
    val apartments: Int,
    /**
     * Спорт
     */
    val gym: Boolean = false,
    /**
     * Для детей
     */
    val childRoom: Boolean = false,
    /**
     * Развлекательные
     */
    val entertainment: Boolean = false,
    /**
     * Магазины
     */
    val shops: Boolean = false,
    /**
     * Co-working
     */
    val coWorking: Boolean = false,
    /**
     * Cap rate (1 st year after compl.)
     */
    val rentalIncome: String? = null,
    /**
     * Страна происхождения застройщика
     */
    val developerCountry: String? = null,
    /**
     * Количество проектов, шт.
     */
    val countProjects: Int? = null,
    /**
     * Количество проектов на этапе строительства, шт.
     */
    val buildProjects: Int? = null,
    /**
     * Количество проектов сданных, шт.
     */
    val soldProjects: Int? = null,
    /**
     * Статус проекта
     */
    val status: Status,
    /**
     * Количество проданных юнитов, шт
     */
    val soldUnits: Int? = null,
    /**
     * Остаток юнитов, шт.
     */
    val availableUnits: Int? = null,
    /**
     * Школы в локации (задать радиус км.)
     */
    val schools: String? = null,
    /**
     * Тип продукта
     */
    val projectType: String? = null,
    /**
     * Средняя цена по проекту
     */
    val avgCost: String? = null,
    /**
     * Средняя стоимость, Виллы
     */
    val avgPrice: String? = null,

    val images: List<String>? = null,
    val projectImage: String? = null,
    val priceStart: Long? = null,
    val priceEnd: Long? = null,
    val floors: Int? = null,
    val type: EstateType,
    val profitAmount: Long? = null,
    val profitTerm: Int? = null,
    val beds: Int? = null,
)

data class Square (
    val studio: SquareParams? = null,
    val oneRoom: SquareParams? = null,
    val twoRooms: SquareParams? = null,
    val threeRooms: SquareParams? = null,
    val fourRooms: SquareParams? = null,
    val fiveRooms: SquareParams? = null,
    val villaTwoRooms: SquareParams? = null,
    val villaThreeRooms: SquareParams? = null,
    val villaFourRooms: SquareParams? = null,
    val villaFiveRooms: SquareParams? = null,
)

data class SquareParams (
    val size: StartEndParams? = null,
    val price: StartEndParams? = null
)

data class StartEndParams (
    val end: String? = null,
    val start: String? = null,
)

enum class EstateType {
    VILLA,
    APARTMENT
}

enum class Status {
    BUILD,
    FINISHED,
}



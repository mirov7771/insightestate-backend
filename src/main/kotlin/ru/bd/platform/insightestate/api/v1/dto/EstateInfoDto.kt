package ru.bd.platform.insightestate.api.v1.dto

import ru.bd.platform.insightestate.entity.EstateType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class EstateInfoDto (
    val id: UUID,
    /**
     *Итоговая оценка
     */
    val rate: String,
    /**
     *Название
     */
    val name: String,
    /**
     *Цена, от
     */
    val priceStart: Long,
    /**
     *Цена, до
     */
    val priceEnd: Long,
    /**
     *Доходность %
     */
    val profitAmount: Long,
    /**
     *Доходность срок
     */
    val profitTerm: Int,
    /**
     *Картинки
     */
    val images: List<String>,
    /**
     *Тип недвижимости
     */
    val type: EstateType,
    /**
     *Площадь, м2, от
     */
    val squareStart: Int,
    /**
     *Площадь, м2, до
     */
    val squareEnd: Int,
    /**
     * Планировка, спальни
     */
    val beds: Int,
    /**
     *Безопасность вложений
     */
    val attachmentSecurity: String,
    /**
     *Инвестиционный потенциал
     */
    val investmentPotential: String,
    /**
     *Расположение объекта
     */
    val locationOfTheObject: String,
    /**
     *Комфорт жизни
     */
    val comfortOfLife: String,
    /**
     * Класс
     */
    val level: String,
    /**
     * Дата сдачи
     */
    val deliveryDate: LocalDate,
    /**
     *Всего этажей
     */
    val floors: Int,
    /**
     *Всего квартир
     */
    val apartments: Int,
    /**
     *До пляжа
     */
    val beach: String,
    /**
     *До аэропорта
     */
    val airport: String,
    /**
     *До торгового центра
     */
    val mall: String,
    /**
     *Парковка
     */
    val parking: String,
    /**
     *Застройщик
     */
    val developer: String,
    /**
     *Детская комната
     */
    val childRoom: Boolean,
    /**
     *Коворкинг
     */
    val coWorking: Boolean,
    /**
     *Спортивный зал
     */
    val gym: Boolean,
    /**
     *ROI за 10 лет
     */
    val roi: BigDecimal,
    /**
     *IRR за 10 лет
     */
    val irr: BigDecimal,
    /**
     *Чистый арендный доход
     */
    val rentalIncome: BigDecimal,
    /**
     *План проекта
     */
    val projectImage: String? = null,
    /**
     * Район
     */
    val district: String,
)

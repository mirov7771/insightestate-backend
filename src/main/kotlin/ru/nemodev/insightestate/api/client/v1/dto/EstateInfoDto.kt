package ru.nemodev.insightestate.api.client.v1.dto

import com.fasterxml.jackson.annotation.JsonInclude
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.insightestate.entity.Square
import java.time.LocalDate
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
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
     *Доходность %
     */
    val profitAmount: Long? = null,
    /**
     *Доходность срок
     */
    val profitTerm: Int? = null,
    /**
     *Картинки
     */
    val images: List<String>,
    /**
     *Тип недвижимости
     */
    val type: EstateType,
    /**
     * Планировка
     */
    val square: Square,
    /**
     * Планировка, спальни
     */
    val beds: Int? = null,
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
    val floors: Int? = null,
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
    val parking: String? = null,
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
    val roi: String? = null,
    /**
     *IRR за 10 лет
     */
    val irr: String? = null,
    /**
     *Чистый арендный доход
     */
    val rentalIncome: String? = null,
    /**
     *План проекта
     */
    val projectImage: String? = null,
    /**
     * Район
     */
    val district: String,
    /**
     * На карте
     */
    val geoPosition: String,
)

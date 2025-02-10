package ru.bd.platform.insightestate.api.v1.dto

import ru.bd.platform.insightestate.entity.EstateType
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
)

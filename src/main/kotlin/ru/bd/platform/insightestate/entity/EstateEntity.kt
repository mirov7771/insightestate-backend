package ru.bd.platform.insightestate.entity

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

@StoreJson
data class EstateDetail (
    val rate: String,
    val name: String,
    val priceStart: Long,
    val priceEnd: Long,
    val profitAmount: Long,
    val profitTerm: Int,
    val images: List<String>,
    val type: EstateType,
    val squareStart: Int,
    val squareEnd: Int,
    val beds: Int,
    val attachmentSecurity: String,
    val investmentPotential: String,
    val locationOfTheObject: String,
    val comfortOfLife: String,
    val level: String,
    val deliveryDate: LocalDate,
    val floors: Int,
    val apartments: Int,
    val beach: String,
    val airport: String,
    val parking: String,
    val developer: String,
    val mall: String,
    val childRoom: Boolean = false,
    val coWorking: Boolean = false,
    val gym: Boolean = false,
    val roi: BigDecimal,
    val irr: BigDecimal,
    val rentalIncome: BigDecimal,
    val projectImage: String? = null,
    val district: String,
)

enum class EstateType {
    VILLA,
    APARTMENT
}



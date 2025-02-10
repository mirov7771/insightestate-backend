package ru.bd.platform.insightestate.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.annotation.StoreJson
import ru.nemodev.platform.core.db.entity.AbstractEntity
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
)

enum class EstateType {
    VILLA,
    APARTMENT
}



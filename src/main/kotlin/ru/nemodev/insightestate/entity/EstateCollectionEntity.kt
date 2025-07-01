package ru.nemodev.insightestate.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.annotation.StoreJson
import ru.nemodev.platform.core.db.entity.AbstractEntity
import java.time.LocalDateTime
import java.util.*

@Table("estate_collections")
class EstateCollectionEntity(
    id: UUID? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = createdAt,

    @Column("collection_detail")
    val collectionDetail: EstateCollectionDetail
) : AbstractEntity<UUID>(id, createdAt, updatedAt)

@StoreJson
data class EstateCollectionDetail(
    val userId: UUID,
    var name: String,
    var estateIds: MutableList<UUID>,
    var unitIds: MutableList<UnitEstateLink>? = null,
)

data class UnitEstateLink (
    val estateId: UUID,
    val unitId: UUID,
)

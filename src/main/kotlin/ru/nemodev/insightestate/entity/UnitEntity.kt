package ru.nemodev.insightestate.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.entity.AbstractEntity
import java.time.LocalDateTime
import java.util.*

@Table("unit")
class UnitEntity (
    id: UUID? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column("code")
    var code: String,
    @Column("corpus")
    var corpus: String? = null,
    @Column("number")
    var number: String? = null,
    @Column("floor")
    var floor: String? = null,
    @Column("rooms")
    var rooms: String? = null,
    @Column("square")
    var square: String? = null,
    @Column("pricesq")
    var priceSq: String? = null,
    @Column("price")
    var price: String? = null,
    @Column("planimage")
    var planImage: String? = null,
) : AbstractEntity<UUID>(id, createdAt, updatedAt)

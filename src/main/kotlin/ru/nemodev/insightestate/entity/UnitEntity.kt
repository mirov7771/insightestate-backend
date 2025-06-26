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
    val code: String,
    @Column("corpus")
    val corpus: String? = null,
    @Column("number")
    val number: String? = null,
    @Column("floor")
    val floor: String? = null,
    @Column("rooms")
    val rooms: String? = null,
    @Column("square")
    val square: String? = null,
    @Column("pricesq")
    val priceSq: String? = null,
    @Column("price")
    val price: String? = null,
    @Column("planimage")
    val planImage: String? = null,
) : AbstractEntity<UUID>(id, createdAt, updatedAt)

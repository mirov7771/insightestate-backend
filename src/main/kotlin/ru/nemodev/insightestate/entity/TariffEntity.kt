package ru.nemodev.insightestate.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.entity.AbstractEntity
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Table("tariff")
class TariffEntity (
    id: UUID? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column("type")
    val type: Int,
    @Column("title")
    val title: String,
    @Column("description")
    val description: String,
    @Column("price")
    val price: BigDecimal,
) : AbstractEntity<UUID>(id, createdAt, updatedAt)

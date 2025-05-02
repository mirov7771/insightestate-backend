package ru.nemodev.insightestate.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.entity.AbstractEntity
import java.time.LocalDateTime
import java.util.*

@Table("subscription")
class SubscriptionEntity (
    id: UUID? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column("user_id")
    val userId: UUID,
    @Column("main_id")
    var mainId: UUID? = null,
    @Column("main_pay_date")
    var mainPayDate: LocalDateTime? = null,
    @Column("extra_id")
    var extraId: UUID? = null,
    @Column("extra_pay_date")
    var extraPayDate: LocalDateTime? = null,
) : AbstractEntity<UUID>(id, createdAt, updatedAt)

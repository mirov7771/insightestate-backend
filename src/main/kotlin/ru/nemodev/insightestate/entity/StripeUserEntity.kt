package ru.nemodev.insightestate.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.entity.AbstractEntity
import java.time.LocalDateTime
import java.util.*

@Table("stripe_user")
class StripeUserEntity (
    id: UUID? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column("user_id")
    val userId: UUID,
    @Column("customer_id")
    val customerId: String,
    @Column("currency")
    val currency: String,
) : AbstractEntity<UUID>(id, createdAt, updatedAt)

package ru.nemodev.insightestate.entity

import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.entity.AbstractEntity
import java.time.LocalDateTime
import java.util.*

@Table("ai_request")
class AiRequestEntity (
    id: UUID? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = createdAt,
    var request: String? = null,
) : AbstractEntity<UUID>(id, createdAt, updatedAt)

package ru.nemodev.insightestate.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.entity.AbstractEntity
import java.time.LocalDateTime
import java.util.*

@Table("likes")
class LikesEntity (
    id: UUID? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column("like_count")
    var likeCount: Long,
    @Column("collection_id")
    var collectionId: UUID,
    @Column("estate_id")
    var estateId: UUID,
) : AbstractEntity<UUID>(id, createdAt, updatedAt)

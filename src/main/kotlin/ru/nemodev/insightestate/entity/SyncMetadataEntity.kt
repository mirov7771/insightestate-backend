package ru.nemodev.insightestate.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.annotation.StoreJson
import ru.nemodev.platform.core.db.entity.AbstractEntity
import java.time.LocalDateTime
import java.util.*

@Table("sync_metadata")
class SyncMetadataEntity(
    id: UUID? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = createdAt,

    @Column("sync_metadata_detail")
    val syncMetadataDetail: SyncMetadataDetail
) : AbstractEntity<UUID>(id, createdAt, updatedAt)

@StoreJson
data class SyncMetadataDetail(
    var airtable: Airtable,
) {
    data class Airtable(
        val country: Country,
        var estateLastUpdatedAt: LocalDateTime, // хранится в UTC
        var unitsLastUpdatedAt: LocalDateTime,  // хранится в UTC
    )
}

enum class Country {
    THAILAND,
    GEORGIA,
    CYPRUS
}

package ru.nemodev.insightestate.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.SyncMetadataEntity
import java.util.*

@Repository
interface SyncMetadataRepository: ListCrudRepository<SyncMetadataEntity, UUID> {

    @Query("""
        select *
        from sync_metadata
        limit 1
        """)
    fun getOne(): SyncMetadataEntity?
}

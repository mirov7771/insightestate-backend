package ru.nemodev.insightestate.service

import org.springframework.stereotype.Service
import ru.nemodev.insightestate.entity.SyncMetadataDetail
import ru.nemodev.insightestate.entity.SyncMetadataEntity
import ru.nemodev.insightestate.repository.SyncMetadataRepository
import java.time.LocalDateTime

interface SyncMetadataService {
    fun getOne(): SyncMetadataEntity
    fun save(syncMetadataEntity: SyncMetadataEntity): SyncMetadataEntity
}

@Service
class SyncMetadataServiceImpl(
    private val repository: SyncMetadataRepository,
) : SyncMetadataService {

    override fun getOne(): SyncMetadataEntity {
        val entity = repository.getOne()
        return entity
            ?: save(SyncMetadataEntity(
                syncMetadataDetail = SyncMetadataDetail(
                    airtable = SyncMetadataDetail.Airtable(
                        estateLastUpdatedAt = LocalDateTime.MIN,
                        unitsLastUpdatedAt = LocalDateTime.MIN
                    )
                )
            ))
    }

    override fun save(syncMetadataEntity: SyncMetadataEntity): SyncMetadataEntity {
        return repository.save(syncMetadataEntity)
    }
}

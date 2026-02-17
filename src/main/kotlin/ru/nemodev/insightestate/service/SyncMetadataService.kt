package ru.nemodev.insightestate.service

import org.springframework.stereotype.Service
import ru.nemodev.insightestate.entity.Country
import ru.nemodev.insightestate.entity.SyncMetadataDetail
import ru.nemodev.insightestate.entity.SyncMetadataEntity
import ru.nemodev.insightestate.repository.SyncMetadataRepository
import java.time.LocalDateTime

interface SyncMetadataService {
    fun getOne(country: Country): SyncMetadataEntity
    fun save(syncMetadataEntity: SyncMetadataEntity): SyncMetadataEntity
}

@Service
class SyncMetadataServiceImpl(
    private val repository: SyncMetadataRepository,
) : SyncMetadataService {

    override fun getOne(
        country: Country
    ): SyncMetadataEntity {
        val entity = repository.getOne(country)
        return entity
            ?: save(SyncMetadataEntity(
                syncMetadataDetail = SyncMetadataDetail(
                    airtable = SyncMetadataDetail.Airtable(
                        estateLastUpdatedAt = LocalDateTime.MIN,
                        unitsLastUpdatedAt = LocalDateTime.MIN,
                        country = country
                    )
                )
            ))
    }

    override fun save(syncMetadataEntity: SyncMetadataEntity): SyncMetadataEntity {
        return repository.save(syncMetadataEntity)
    }
}

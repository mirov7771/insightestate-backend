package ru.nemodev.insightestate.service.airtable

import org.springframework.stereotype.Service
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.insightestate.entity.UnitEntity
import ru.nemodev.insightestate.integration.airtable.AirtableIntegration
import ru.nemodev.insightestate.integration.airtable.dto.EstateRecordsDtoRs
import ru.nemodev.insightestate.integration.airtable.dto.UnitRecordsDtoRs
import ru.nemodev.insightestate.repository.UnitRepository
import ru.nemodev.insightestate.service.SyncMetadataService
import ru.nemodev.insightestate.service.estate.EstateService
import java.time.LocalDateTime

interface AirtableService {
    fun refreshEstateData()
}

@Service
class AirtableServiceImpl(
    private val airtableIntegration: AirtableIntegration,
    private val estateAirtableMapper: EstateAirtableMapper,
    private val unitAirtableMapper: UnitAirtableMapper,
    private val estateService: EstateService,
    private val syncMetadataService: SyncMetadataService,
    private val unitRepository: UnitRepository,
) : AirtableService {

    companion object {
        private const val REFRESH_BATCH_SIZE = 30
    }

    override fun refreshEstateData() {
        refreshEstate()
        refreshUnits()
    }

    private fun refreshEstate() {
        val syncMetadata = syncMetadataService.getOne()
        val estateLastUpdatedAt = syncMetadata.syncMetadataDetail.airtable.estateLastUpdatedAt
        var newLastUpdatedAt = estateLastUpdatedAt
        val estateForUpdate = mutableListOf<EstateEntity>()
        var estateFromAirtable = estateRecords(
            updatedAt = estateLastUpdatedAt,
            pageSize = REFRESH_BATCH_SIZE
        )

        var offset = estateFromAirtable.offset
        var estateFromAirtableRecords = estateFromAirtable.records

        while (estateFromAirtableRecords.isNotEmpty()) {
            val existsEstateByProjectMap = estateService.findAllByProjectIds(estateFromAirtableRecords.map { it.fields.projectId } )
                .associateBy { it.estateDetail.projectId }

            estateFromAirtableRecords.forEach { estateFromAirtable ->
                val estate = existsEstateByProjectMap[estateFromAirtable.fields.projectId]

                if (estate == null) {
                    estateForUpdate.add(estateAirtableMapper.mapToEntity(estateFromAirtable.fields))
                } else {
                    val facilityImages = estate.estateDetail.facilityImages
                    val exteriorImages = estate.estateDetail.exteriorImages
                    val interiorImages = estate.estateDetail.interiorImages
                    estate.estateDetail = estateAirtableMapper.mapToEntity(estateFromAirtable.fields).estateDetail
                    estate.estateDetail.facilityImages = facilityImages
                    estate.estateDetail.exteriorImages = exteriorImages
                    estate.estateDetail.interiorImages = interiorImages

                    estateForUpdate.add(estate)
                }

                if (estateFromAirtable.fields.updatedAt > newLastUpdatedAt) {
                    newLastUpdatedAt = estateFromAirtable.fields.updatedAt
                }
            }

            // Скрываем объекты без фото
            estateForUpdate.forEach {
                it.estateDetail.canShow = it.isCanShow()
            }

            estateService.saveAll(estateForUpdate)

            if (offset == null) {
                estateFromAirtableRecords = emptyList()
            } else {
                estateFromAirtable = estateRecords(
                    updatedAt = estateLastUpdatedAt,
                    pageSize = REFRESH_BATCH_SIZE,
                    offset = offset,
                )
                offset = estateFromAirtable.offset
                estateFromAirtableRecords = estateFromAirtable.records
            }
        }

        syncMetadata.syncMetadataDetail.airtable.estateLastUpdatedAt = newLastUpdatedAt
        syncMetadataService.save(syncMetadata)
    }

    private fun refreshUnits() {
        val syncMetadata = syncMetadataService.getOne()
        val unitsLastUpdatedAt = syncMetadata.syncMetadataDetail.airtable.unitsLastUpdatedAt
        var newLastUpdatedAt = unitsLastUpdatedAt
        val unitsForUpdate = mutableListOf<UnitEntity>()
        var unitsFromAirtable = unitRecords(
            updatedAt = unitsLastUpdatedAt,
            pageSize = REFRESH_BATCH_SIZE
        )

        var offset = unitsFromAirtable.offset
        var unitsFromAirtableRecords = unitsFromAirtable.records

        while (unitsFromAirtableRecords.isNotEmpty()) {
            val existsUnitsByCodeMap = unitRepository.findAllByCodes(unitsFromAirtableRecords.map { it.fields.unitId } )
                .associateBy { it.code }

            unitsFromAirtableRecords.forEach { unitFromAirtable ->
                val unit = existsUnitsByCodeMap[unitFromAirtable.fields.unitId]

                if (unit == null) {
                    unitsForUpdate.add(unitAirtableMapper.mapToEntity(unitFromAirtable.fields, null))
                } else {
                    unitsForUpdate.add(unitAirtableMapper.mapToEntity(unitFromAirtable.fields, unit))
                }

                if (unitFromAirtable.fields.updatedAt > newLastUpdatedAt) {
                    newLastUpdatedAt = unitFromAirtable.fields.updatedAt
                }
            }

            unitRepository.saveAll(unitsForUpdate)

            if (offset == null) {
                unitsFromAirtableRecords = emptyList()
            } else {
                unitsFromAirtable = unitRecords(
                    updatedAt = unitsLastUpdatedAt,
                    pageSize = REFRESH_BATCH_SIZE,
                    offset = offset,
                )
                offset = unitsFromAirtable.offset
                unitsFromAirtableRecords = unitsFromAirtable.records
            }
        }

        syncMetadata.syncMetadataDetail.airtable.unitsLastUpdatedAt = newLastUpdatedAt
        syncMetadataService.save(syncMetadata)
    }

    private fun estateRecords(
        updatedAt: LocalDateTime,
        pageSize: Int? = null,
        offset: String? = null
    ): EstateRecordsDtoRs {
        return airtableIntegration.estateRecords(
            updatedAt,
            pageSize,
            offset
        )
    }

    private fun unitRecords(
        updatedAt: LocalDateTime,
        pageSize: Int? = null,
        offset: String? = null
    ): UnitRecordsDtoRs {
        return airtableIntegration.unitRecords(
            updatedAt,
            pageSize,
            offset
        )
    }
}

package ru.nemodev.insightestate.service.airtable

import org.springframework.stereotype.Service
import ru.nemodev.insightestate.config.integration.AirtableProperties
import ru.nemodev.insightestate.entity.Country
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.insightestate.entity.UnitEntity
import ru.nemodev.insightestate.integration.airtable.AirtableIntegration
import ru.nemodev.insightestate.integration.airtable.dto.EstateRecordsDtoRs
import ru.nemodev.insightestate.integration.airtable.dto.UnitRecordsDtoRs
import ru.nemodev.insightestate.repository.UnitRepository
import ru.nemodev.insightestate.service.SyncMetadataService
import ru.nemodev.insightestate.service.estate.EstateService
import ru.nemodev.platform.core.logging.sl4j.Loggable
import java.time.LocalDateTime

interface AirtableService {
    fun refreshEstateData(country: Country)
    fun deleteFromAirtable()
}

@Service
class AirtableServiceImpl(
    private val airtableIntegration: AirtableIntegration,
    private val estateAirtableMapper: EstateAirtableMapper,
    private val unitAirtableMapper: UnitAirtableMapper,
    private val estateService: EstateService,
    private val syncMetadataService: SyncMetadataService,
    private val unitRepository: UnitRepository,
    private val airtableProperties: AirtableProperties,
) : AirtableService {

    companion object : Loggable {
        private const val BATCH_SIZE = 30
    }

    override fun refreshEstateData(
        country: Country
    ) {
        refreshEstate(country)
        refreshUnits(country)
    }

    private fun refreshEstate(
        country: Country
    ) {
        val syncMetadata = syncMetadataService.getOne(country)
        val estateLastUpdatedAt = syncMetadata.syncMetadataDetail.airtable.estateLastUpdatedAt
        var newLastUpdatedAt = estateLastUpdatedAt
        val estateForUpdate = mutableListOf<EstateEntity>()
        var estateFromAirtable = estateRecords(
            pageSize = BATCH_SIZE,
            country = country,
            filterByFormula = buildFilterByFormula(estateLastUpdatedAt)
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
                    pageSize = BATCH_SIZE,
                    offset = offset,
                    country = country,
                    filterByFormula = buildFilterByFormula(estateLastUpdatedAt)
                )
                offset = estateFromAirtable.offset
                estateFromAirtableRecords = estateFromAirtable.records
            }
        }

        syncMetadata.syncMetadataDetail.airtable.estateLastUpdatedAt = newLastUpdatedAt
        syncMetadataService.save(syncMetadata)
    }

    private fun refreshUnits(
        country: Country
    ) {
        val syncMetadata = syncMetadataService.getOne(country)
        val unitsLastUpdatedAt = syncMetadata.syncMetadataDetail.airtable.unitsLastUpdatedAt
        var newLastUpdatedAt = unitsLastUpdatedAt
        val unitsForUpdate = mutableListOf<UnitEntity>()
        var unitsFromAirtable = unitRecords(
            pageSize = BATCH_SIZE,
            country = country,
            filterByFormula = buildFilterByFormula(unitsLastUpdatedAt)
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
                    pageSize = BATCH_SIZE,
                    offset = offset,
                    country = country,
                    filterByFormula = buildFilterByFormula(unitsLastUpdatedAt)
                )
                offset = unitsFromAirtable.offset
                unitsFromAirtableRecords = unitsFromAirtable.records
            }
        }

        syncMetadata.syncMetadataDetail.airtable.unitsLastUpdatedAt = newLastUpdatedAt
        syncMetadataService.save(syncMetadata)
    }

    private fun estateRecords(
        pageSize: Int? = null,
        offset: String? = null,
        country: Country,
        filterByFormula: String
    ): EstateRecordsDtoRs {
        return airtableIntegration.estateRecords(
            pageSize,
            offset,
            country,
            filterByFormula
        )
    }

    private fun unitRecords(
        pageSize: Int? = null,
        offset: String? = null,
        country: Country,
        filterByFormula: String
    ): UnitRecordsDtoRs {
        return airtableIntegration.unitRecords(
            pageSize,
            offset,
            country,
            filterByFormula
        )
    }

    override fun deleteFromAirtable() {
        airtableProperties.countriesForDelete.forEach { country ->
            logInfo { "Начало удаление объектов недвижимости из airtable $country" }

            deleteEstate(country)
            deleteUnits(country)

            logInfo { "Закончили удаление объектов недвижимости из airtable $country" }
        }
    }

    private fun deleteEstate(country: Country) {
        var estateForDelete = estateRecords(
            pageSize = BATCH_SIZE,
            country = country,
            filterByFormula = "not(active)"
        )

        var offset = estateForDelete.offset
        var estateRecords = estateForDelete.records

        while (estateRecords.isNotEmpty()) {
            val deletedCount = estateService.deleteByProjectIds(estateRecords.map { it.fields.projectId })
            logInfo { "$deletedCount объектов удалено из БД" }

            if (offset == null) {
                estateRecords = emptyList()
            } else {
                estateForDelete = estateRecords(
                    offset = offset,
                    pageSize = BATCH_SIZE,
                    country = country,
                    filterByFormula = "delete"
                )

                offset = estateForDelete.offset
                estateRecords = estateForDelete.records
            }
        }
    }

    private fun deleteUnits(country: Country) {
        var unitsForDelete = unitRecords(
            pageSize = BATCH_SIZE,
            country = country,
            filterByFormula = "not(active)"
        )

        var offset = unitsForDelete.offset
        var unitRecords = unitsForDelete.records

        while (unitRecords.isNotEmpty()) {
            val deletedCount = unitRepository.deleteByCodes(unitRecords.map { it.fields.unitId })
            logInfo { "$deletedCount юнитов удалено из БД" }

            if (offset == null) {
                unitRecords = emptyList()
            } else {
                unitsForDelete = unitRecords(
                    offset = offset,
                    pageSize = BATCH_SIZE,
                    country = country,
                    filterByFormula = "delete"
                )

                offset = unitsForDelete.offset
                unitRecords = unitsForDelete.records
            }
        }
    }

    private fun buildFilterByFormula(updatedAt: LocalDateTime): String {
        return "AND(updatedAt > '${updatedAt}', active)"
    }
}

package ru.nemodev.insightestate.api.client.v1.processor

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.converter.EstateCollectionDtoRsConverter
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateCollectionCreateDtoRq
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateCollectionCreateDtoRs
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateCollectionDtoRs
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateCollectionUpdateDto
import ru.nemodev.insightestate.domen.EstateCollection
import ru.nemodev.insightestate.service.estate.EstateCollectionService
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
import java.util.*

interface EstateCollectionProcessor {
    fun findAll(authBasicToken: String, pageable: Pageable): PageDtoRs<EstateCollectionDtoRs>
    fun create(authBasicToken: String, request: EstateCollectionCreateDtoRq): EstateCollectionCreateDtoRs
    fun addEstateToCollection(authBasicToken: String, id: UUID, estateId: UUID)
    fun deleteEstateFromCollection(authBasicToken: String, id: UUID, estateId: UUID)
    fun deleteById(authBasicToken: String, id: UUID)
    fun getById(id: UUID): EstateCollectionDtoRs
    fun update(id: UUID, rq: EstateCollectionUpdateDto)
}

@Component
class EstateCollectionProcessorImpl(
    private val estateCollectionService: EstateCollectionService,
    private val estateCollectionDtoRsConverter: EstateCollectionDtoRsConverter
) : EstateCollectionProcessor {

    override fun findAll(authBasicToken: String, pageable: Pageable): PageDtoRs<EstateCollectionDtoRs> {
        val estateCollections = estateCollectionService.findAll(authBasicToken, pageable)

        return PageDtoRs(
            items = estateCollections.map { estateCollectionDtoRsConverter.convert(it) },
            pageSize = estateCollections.size,
            pageNumber = pageable.pageNumber,
            hasMore = estateCollections.size >= pageable.pageSize
        )
    }

    override fun create(authBasicToken: String, request: EstateCollectionCreateDtoRq): EstateCollectionCreateDtoRs {
        val estateCollectionEntity = estateCollectionService.create(authBasicToken, request)
        return EstateCollectionCreateDtoRs(
            id = estateCollectionEntity.id
        )
    }

    override fun addEstateToCollection(authBasicToken: String, id: UUID, estateId: UUID) {
        estateCollectionService.addEstateToCollection(
            authBasicToken = authBasicToken,
            id = id,
            estateId = estateId
        )
    }

    override fun deleteEstateFromCollection(authBasicToken: String, id: UUID, estateId: UUID) {
        estateCollectionService.deleteEstateFromCollection(
            authBasicToken = authBasicToken,
            id = id,
            estateId = estateId
        )
    }

    override fun deleteById(authBasicToken: String, id: UUID) {
        estateCollectionService.deleteById(authBasicToken, id)
    }

    override fun getById(id: UUID): EstateCollectionDtoRs {
        val entity = estateCollectionService.findById(id)
        return estateCollectionDtoRsConverter.convert(
            EstateCollection(
                estateCollection = entity,
                estates = estateCollectionService.findEstates(entity.collectionDetail.estateIds.toSet())
            )
        )
    }

    override fun update(id: UUID, rq: EstateCollectionUpdateDto) {
        val entity = estateCollectionService.findById(id)
        entity.collectionDetail.name = rq.name
        estateCollectionService.update(entity)
    }
}

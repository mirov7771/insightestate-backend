package ru.nemodev.insightestate.api.client.v1.processor

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.converter.EstateCollectionDtoRsConverter
import ru.nemodev.insightestate.api.client.v1.dto.estate.*
import ru.nemodev.insightestate.domen.EstateCollection
import ru.nemodev.insightestate.entity.LikesEntity
import ru.nemodev.insightestate.integration.cutt.CuttIntegration
import ru.nemodev.insightestate.repository.LikesRepository
import ru.nemodev.insightestate.service.EmailService
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
    fun short(rq: ShortDto): ShortDto
    fun saveLike(rq: LikeDto)
}

@Component
class EstateCollectionProcessorImpl(
    private val estateCollectionService: EstateCollectionService,
    private val estateCollectionDtoRsConverter: EstateCollectionDtoRsConverter,
    private val cuttIntegration: CuttIntegration,
    private val likesRepository: LikesRepository,
    private val emailService: EmailService,
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
        val estates = estateCollectionService.findEstates(entity.collectionDetail.estateIds.toSet())
        estates.forEach { estate ->
            estate.estateDetail.likesCount =
                likesRepository.findByCollectionIdAndEstateId(id, estate.id).firstOrNull()?.likeCount
        }
        return estateCollectionDtoRsConverter.convert(
            EstateCollection(
                estateCollection = entity,
                estates = estates
            )
        )
    }

    override fun update(id: UUID, rq: EstateCollectionUpdateDto) {
        val entity = estateCollectionService.findById(id)
        entity.collectionDetail.name = rq.name
        estateCollectionService.update(entity)
    }

    override fun short(rq: ShortDto): ShortDto {
        return ShortDto(
            url = cuttIntegration.short(rq.url) ?: rq.url,
        )
    }

    override fun saveLike(rq: LikeDto) {
        var likeDao = likesRepository.findByCollectionIdAndEstateId(rq.collectionId, rq.estateId).firstOrNull()
        if (likeDao != null) {
            likeDao.likeCount += 1L
            likesRepository.save(likeDao.apply { isNew = false })
        } else {
            likeDao = LikesEntity(
                id = UUID.randomUUID(),
                collectionId = rq.collectionId,
                estateId = rq.estateId,
                likeCount = 1L
            )
            likesRepository.save(likeDao.apply { isNew = true })
        }
        emailService.sendMessage(
            email = rq.email,
            subject = "Вашему клиенту понравился объект из подборки",
            message = "Объект: ${rq.title}\nПодборка: ${rq.collection}\n${rq.url}"
        )
    }
}

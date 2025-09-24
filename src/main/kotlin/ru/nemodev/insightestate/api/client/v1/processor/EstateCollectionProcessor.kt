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
import ru.nemodev.insightestate.service.UserService
import ru.nemodev.insightestate.service.estate.EstateCollectionService
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
import ru.nemodev.platform.core.extensions.isNotNullOrEmpty
import java.util.*

interface EstateCollectionProcessor {
    fun findAll(authBasicToken: String, pageable: Pageable): PageDtoRs<EstateCollectionDtoRs>
    fun create(authBasicToken: String, request: EstateCollectionCreateDtoRq): EstateCollectionCreateDtoRs
    fun addEstateToCollection(authBasicToken: String, id: UUID, estateId: UUID, unitId: UUID?)
    fun deleteEstateFromCollection(authBasicToken: String, id: UUID, estateId: UUID)
    fun deleteById(authBasicToken: String, id: UUID)
    fun getById(id: UUID): EstateCollectionDtoRs
    fun update(id: UUID, rq: EstateCollectionUpdateDto)
    fun short(rq: ShortDto): ShortDto
    fun saveLike(rq: LikeDto)
    fun template(rq: TemplateRq): TemplateRs
}

@Component
class EstateCollectionProcessorImpl(
    private val estateCollectionService: EstateCollectionService,
    private val estateCollectionDtoRsConverter: EstateCollectionDtoRsConverter,
    private val cuttIntegration: CuttIntegration,
    private val likesRepository: LikesRepository,
    private val emailService: EmailService,
    private val userService: UserService,
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

    override fun addEstateToCollection(authBasicToken: String, id: UUID, estateId: UUID, unitId: UUID?) {
        estateCollectionService.addEstateToCollection(
            authBasicToken = authBasicToken,
            id = id,
            estateId = estateId,
            unitId = unitId,
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
        val estates = estateCollectionService.findEstates(entity.collectionDetail.estateIds.toSet()).toMutableList()

        if (entity.collectionDetail.unitIds.isNotNullOrEmpty()) {
            val list = estateCollectionService.findEstatesWithUnites(entity.collectionDetail.unitIds!!)
            if (list.isNotEmpty()) {
                estates.addAll(list)
            }
        }

        estates.forEach { estate ->
            estate.estateDetail.likesCount =
                likesRepository.findByCollectionIdAndEstateId(id, estate.id).firstOrNull()?.likeCount
        }

        val rs = estateCollectionDtoRsConverter.convert(
            EstateCollection(
                estateCollection = entity,
                estates = estates
            )
        )
        rs.agentInfo = userService.getUserById(entity.collectionDetail.userId)
        return rs
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
            subject = "Your client liked a property from your collection!",
            message = "Property: ${rq.title}\nCollection: ${rq.collection}\n${rq.url}"
        )
    }

    override fun template(rq: TemplateRq): TemplateRs {
        val id = when (rq.id) {
            //Лидеры продаж — Пхукет/Top sellers — Phuket
            1 -> estateCollectionService.create(
                userId = rq.userId,
                ids = listOf(
                    UUID.fromString("01952e8b-1c95-74ee-bf85-5c7c382a3709"),
                    UUID.fromString("01952e8b-1ca5-722a-9e05-8db5625e1631"),
                    UUID.fromString("01952e8b-1c93-797f-81ab-80204041988a"),
                    UUID.fromString("01960bbc-d39e-71d9-a45a-6d8e4c4ad1da"),
                    UUID.fromString("01960bbc-d39e-71d9-a45a-6d8e4c4ad1d3"),
                    UUID.fromString("01952e8b-1c7f-7a5e-883b-b2013ecfdf11"),
                    UUID.fromString("01952e8b-1ca0-70a2-aa2e-1d62b6e0eb49"),
                    UUID.fromString("01952e8b-1c85-71b8-9446-09226b121b1a"),
                    UUID.fromString("01952e8b-1c7a-7422-b9ac-8b091eaeeac6"),
                    UUID.fromString("01952e8b-1c94-75d1-9d17-b26919f3fb50"),
                ),
                template = rq.template
            ).id
            //Выбор платформы — Виллы/Platform’s choice — Villas
            2 -> estateCollectionService.create(
                userId = rq.userId,
                ids = listOf(
                    UUID.fromString("0198a285-ed29-7a15-851a-75c8553720cd"),
                    UUID.fromString("01952e8b-1c74-7443-9809-bbbfe626e28e"),
                    UUID.fromString("01952e8b-1c82-772b-b90a-00a393e4790a"),
                    UUID.fromString("01952e8b-1c94-75d1-9d17-b26919f3fb51"),
                ),
                template = rq.template
            ).id
            //Первая линия/First line
            3 -> estateCollectionService.create(
                userId = rq.userId,
                ids = listOf(
                    UUID.fromString("01960bbc-d39a-7e34-bbec-a21c7c1e21a8"),
                    UUID.fromString("01996238-4101-778f-945d-b76b22c4fffb"),
                    UUID.fromString("01996238-4102-79a0-8a3e-a3978f38bb95"),
                    UUID.fromString("0198e6d3-2039-79b5-99cb-1fe671e273b0"),
                    UUID.fromString("01952e8b-1c98-7072-bbc1-0084b629e068"),
                    UUID.fromString("01952e8b-1c8a-7172-a6e9-2f5b1dcf321e"),
                    UUID.fromString("01952e8b-1c85-71b8-9446-09226b121b1a"),
                    UUID.fromString("01952e8b-1c85-71b8-9446-09226b121b19"),
                    UUID.fromString("01952e8b-1ca3-7675-b6e8-de49cf1a8401"),
                    UUID.fromString("01952e8b-1c8e-7773-bcc7-f1fa7123fa30"),
                ),
                template = rq.template
            ).id
            //Выбор платформы — Кондо/Platform’s choice — Condos
            4 -> estateCollectionService.create(
                userId = rq.userId,
                ids = listOf(
                    UUID.fromString("01952e8b-1c88-7807-8ff7-3ecb6820ace8"),
                    UUID.fromString("01952e8b-1c9a-7dd0-85b9-cab80f0123aa"),
                    UUID.fromString("01952e8b-1c7f-7a5e-883b-b2013ecfdf11"),
                    UUID.fromString("01952e8b-1c87-7625-83bb-3fcbd4d20eb2"),
                    UUID.fromString("01952e8b-1ca0-70a2-aa2e-1d62b6e0eb49"),
                    UUID.fromString("01952e8b-1c85-71b8-9446-09226b121b1a"),
                    UUID.fromString("01952e8b-1c94-75d1-9d17-b26919f3fb50"),
                    UUID.fromString("01952e8b-1c9b-7006-a52c-5e4412e5eb10"),
                    UUID.fromString("01952e8b-1c7f-7a5e-883b-b2013ecfdf12"),
                    UUID.fromString("01952e8b-1c97-781b-b492-b08314b4c386"),
                ),
                template = rq.template
            ).id
            //Кондо до 150 000$/Condos up to $150,000
            5 -> estateCollectionService.create(
                userId = rq.userId,
                ids = listOf(
                    UUID.fromString("01952e8b-1c95-74ee-bf85-5c7c382a3709"),
                    UUID.fromString("01952e8b-1c94-75d1-9d17-b26919f3fb52"),
                    UUID.fromString("01952e8b-1c88-7807-8ff7-3ecb6820ace8"),
                    UUID.fromString("01952e8b-1c7f-7a5e-883b-b2013ecfdf11"),
                    UUID.fromString("01952e8b-1ca0-70a2-aa2e-1d62b6e0eb49"),
                    UUID.fromString("01952e8b-1c7a-7422-b9ac-8b091eaeeac5"),
                    UUID.fromString("01952e8b-1c8d-7aef-ad95-3c94ecc219f9"),
                    UUID.fromString("01952e8b-1c94-75d1-9d17-b26919f3fb50"),
                    UUID.fromString("01952e8b-1c9b-7006-a52c-5e4412e5eb10"),
                    UUID.fromString("01952e8b-1ca4-7dee-bfb6-cc5f542ef09f"),
                ),
                template = rq.template
            ).id
            //Для семьи — школы и сады рядом/For families — schools and kindergartens nearby
            6 -> estateCollectionService.create(
                userId = rq.userId,
                ids = listOf(
                    UUID.fromString("01952e8b-1c8a-7172-a6e9-2f5b1dcf321d"),
                    UUID.fromString("01952e8b-1c90-74a8-acec-5d9b8172b4dc"),
                    UUID.fromString("01952e8b-1c94-75d1-9d17-b26919f3fb51"),
                    UUID.fromString("01960bbc-d39d-7034-a858-79b43974baf6"),
                    UUID.fromString("01952e8b-1c9d-7438-9ba1-3126c32ddf99"),
                    UUID.fromString("01952e8b-1ca4-7dee-bfb6-cc5f542ef09f"),
                    UUID.fromString("0198a285-ed33-74d3-9df9-20aef4e04cb1"),
                    UUID.fromString("0198e6d3-2036-7e78-9ce2-b120145f5a76"),
                    UUID.fromString("01996238-4101-778f-945d-b76b22c4fffa"),
                ),
                template = rq.template
            ).id
            //Выбор наших пользователей/Our users’ choice
            else -> estateCollectionService.create(
                userId = rq.userId,
                ids = listOf(
                    UUID.fromString("01952e8b-1c94-75d1-9d17-b26919f3fb52"),
                    UUID.fromString("01952e8b-1ca5-722a-9e05-8db5625e1631"),
                    UUID.fromString("01952e8b-1c93-797f-81ab-80204041988a"),
                    UUID.fromString("01960bbc-d39e-71d9-a45a-6d8e4c4ad1da"),
                    UUID.fromString("01952e8b-1c88-7807-8ff7-3ecb6820ace8"),
                    UUID.fromString("01952e8b-1c7f-7a5e-883b-b2013ecfdf11"),
                    UUID.fromString("01952e8b-1c94-75d1-9d17-b26919f3fb50"),
                    UUID.fromString("01952e8b-1c9b-7006-a52c-5e4412e5eb10"),
                    UUID.fromString("01952e8b-1ca8-72fb-94ea-dc8a2f9df3b5"),
                    UUID.fromString("01952e8b-1c97-781b-b492-b08314b4c386"),
                ),
                template = rq.template
            ).id
        }
        return TemplateRs(id)
    }
}

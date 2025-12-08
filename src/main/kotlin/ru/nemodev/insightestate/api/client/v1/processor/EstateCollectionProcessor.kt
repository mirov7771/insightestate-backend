package ru.nemodev.insightestate.api.client.v1.processor

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.converter.EstateCollectionDtoRsConverter
import ru.nemodev.insightestate.api.client.v1.dto.estate.*
import ru.nemodev.insightestate.api.client.v1.processor.EstateProcessorImpl.Companion
import ru.nemodev.insightestate.domen.EstateCollection
import ru.nemodev.insightestate.entity.LikesEntity
import ru.nemodev.insightestate.integration.currency.CurrencyService
import ru.nemodev.insightestate.integration.cutt.CuttIntegration
import ru.nemodev.insightestate.repository.LikesRepository
import ru.nemodev.insightestate.service.EmailService
import ru.nemodev.insightestate.service.UserService
import ru.nemodev.insightestate.service.estate.EstateCollectionService
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
import ru.nemodev.platform.core.extensions.isNotNullOrEmpty
import ru.nemodev.platform.core.extensions.nullIfEmpty
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

interface EstateCollectionProcessor {
    fun findAll(currency: String? = null, authBasicToken: String, pageable: Pageable): PageDtoRs<EstateCollectionDtoRs>
    fun create(authBasicToken: String, request: EstateCollectionCreateDtoRq): EstateCollectionCreateDtoRs
    fun addEstateToCollection(authBasicToken: String, id: UUID, estateId: UUID, unitId: UUID?)
    fun deleteEstateFromCollection(authBasicToken: String, id: UUID, estateId: UUID)
    fun deleteById(authBasicToken: String, id: UUID)
    fun getById(currency: String? = null, id: UUID): EstateCollectionDtoRs
    fun update(id: UUID, rq: EstateCollectionUpdateDto)
    fun short(rq: ShortDto): ShortDto
    fun saveLike(rq: LikeDto)
    fun template(rq: TemplateRq): TemplateRs
    fun duplicate(id: UUID)
    fun activity(rq: ActivityDto)
}

@Component
class EstateCollectionProcessorImpl(
    private val estateCollectionService: EstateCollectionService,
    private val estateCollectionDtoRsConverter: EstateCollectionDtoRsConverter,
    private val cuttIntegration: CuttIntegration,
    private val likesRepository: LikesRepository,
    private val emailService: EmailService,
    private val userService: UserService,
    private val currencyService: CurrencyService,
) : EstateCollectionProcessor {

    companion object {
        val dec = DecimalFormat("#,###")
    }

    override fun findAll(currency: String?, authBasicToken: String, pageable: Pageable): PageDtoRs<EstateCollectionDtoRs> {
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

    override fun getById(currency: String?, id: UUID): EstateCollectionDtoRs {
        val entity = estateCollectionService.findById(id)
        val estates = estateCollectionService.findEstates(entity.collectionDetail.estateIds.toSet()).toMutableList()

        if (entity.collectionDetail.unitIds.isNotNullOrEmpty()) {
            val list = estateCollectionService.findEstatesWithUnites(entity.collectionDetail.unitIds!!)
            if (list.isNotEmpty()) {
                estates.addAll(list)
            }
        }

        if (currency != null && currency != "THB") {
            estates.forEach {
                it.estateDetail.price.min = getPrice(it.estateDetail.price.min, currency)!!
                it.estateDetail.price.max = getPrice(it.estateDetail.price.max, currency)!!
                it.estateDetail.price.avg = getPrice(it.estateDetail.price.avg, currency)

                if (it.estateDetail.roomLayouts.one?.price != null) {
                    it.estateDetail.roomLayouts.one?.price!!.min = getPrice(it.estateDetail.roomLayouts.one?.price!!.min, currency)!!
                    it.estateDetail.roomLayouts.one?.price!!.max = getPrice(it.estateDetail.roomLayouts.one?.price!!.max, currency)!!
                    if (it.estateDetail.roomLayouts.one?.price?.avg != null)
                        it.estateDetail.roomLayouts.one?.price!!.avg = getPrice(it.estateDetail.roomLayouts.one?.price?.avg, currency)
                }
                if (it.estateDetail.roomLayouts.two?.price != null) {
                    it.estateDetail.roomLayouts.two?.price!!.min = getPrice(it.estateDetail.roomLayouts.two?.price!!.min, currency)!!
                    it.estateDetail.roomLayouts.two?.price!!.max = getPrice(it.estateDetail.roomLayouts.two?.price!!.max, currency)!!
                    if (it.estateDetail.roomLayouts.two?.price?.avg != null)
                        it.estateDetail.roomLayouts.two?.price!!.avg = getPrice(it.estateDetail.roomLayouts.two?.price?.avg, currency)
                }
                if (it.estateDetail.roomLayouts.three?.price != null) {
                    it.estateDetail.roomLayouts.three?.price!!.min = getPrice(it.estateDetail.roomLayouts.three?.price!!.min, currency)!!
                    it.estateDetail.roomLayouts.three?.price!!.max = getPrice(it.estateDetail.roomLayouts.three?.price!!.max, currency)!!
                    if (it.estateDetail.roomLayouts.three?.price?.avg != null)
                        it.estateDetail.roomLayouts.three?.price!!.avg = getPrice(it.estateDetail.roomLayouts.three?.price?.avg, currency)
                }
                if (it.estateDetail.roomLayouts.four?.price != null) {
                    it.estateDetail.roomLayouts.four?.price!!.min = getPrice(it.estateDetail.roomLayouts.four?.price!!.min, currency)!!
                    it.estateDetail.roomLayouts.four?.price!!.max = getPrice(it.estateDetail.roomLayouts.four?.price!!.max, currency)!!
                    if (it.estateDetail.roomLayouts.four?.price?.avg != null)
                        it.estateDetail.roomLayouts.four?.price!!.avg = getPrice(it.estateDetail.roomLayouts.four?.price?.avg, currency)
                }
                if (it.estateDetail.roomLayouts.five?.price != null) {
                    it.estateDetail.roomLayouts.five?.price!!.min = getPrice(it.estateDetail.roomLayouts.five?.price!!.min, currency)!!
                    it.estateDetail.roomLayouts.five?.price!!.max = getPrice(it.estateDetail.roomLayouts.five?.price!!.max, currency)!!
                    if (it.estateDetail.roomLayouts.five?.price?.avg != null)
                        it.estateDetail.roomLayouts.five?.price!!.avg = getPrice(it.estateDetail.roomLayouts.five?.price?.avg, currency)
                }
                if (it.estateDetail.roomLayouts.studio?.price != null) {
                    it.estateDetail.roomLayouts.studio?.price!!.min = getPrice(it.estateDetail.roomLayouts.studio?.price!!.min, currency)!!
                    it.estateDetail.roomLayouts.studio?.price!!.max = getPrice(it.estateDetail.roomLayouts.studio?.price!!.max, currency)!!
                    if (it.estateDetail.roomLayouts.studio?.price?.avg != null)
                        it.estateDetail.roomLayouts.studio?.price!!.avg = getPrice(it.estateDetail.roomLayouts.studio?.price?.avg, currency)
                }
                if (it.estateDetail.roomLayouts.villaTwo?.price != null) {
                    it.estateDetail.roomLayouts.villaTwo?.price!!.min = getPrice(it.estateDetail.roomLayouts.villaTwo?.price!!.min, currency)!!
                    it.estateDetail.roomLayouts.villaTwo?.price!!.max = getPrice(it.estateDetail.roomLayouts.villaTwo?.price!!.max, currency)!!
                    if (it.estateDetail.roomLayouts.villaTwo?.price?.avg != null)
                        it.estateDetail.roomLayouts.villaTwo?.price!!.avg = getPrice(it.estateDetail.roomLayouts.villaTwo?.price?.avg, currency)
                }
                if (it.estateDetail.roomLayouts.villaThree?.price != null) {
                    it.estateDetail.roomLayouts.villaThree?.price!!.min = getPrice(it.estateDetail.roomLayouts.villaThree?.price!!.min, currency)!!
                    it.estateDetail.roomLayouts.villaThree?.price!!.max = getPrice(it.estateDetail.roomLayouts.villaThree?.price!!.max, currency)!!
                    if (it.estateDetail.roomLayouts.villaThree?.price?.avg != null)
                        it.estateDetail.roomLayouts.villaThree?.price!!.avg = getPrice(it.estateDetail.roomLayouts.villaThree?.price?.avg, currency)
                }
                if (it.estateDetail.roomLayouts.villaFour?.price != null) {
                    it.estateDetail.roomLayouts.villaFour?.price!!.min = getPrice(it.estateDetail.roomLayouts.villaFour?.price!!.min, currency)!!
                    it.estateDetail.roomLayouts.villaFour?.price!!.max = getPrice(it.estateDetail.roomLayouts.villaFour?.price!!.max, currency)!!
                    if (it.estateDetail.roomLayouts.villaFour?.price?.avg != null)
                        it.estateDetail.roomLayouts.villaFour?.price!!.avg = getPrice(it.estateDetail.roomLayouts.villaFour?.price?.avg, currency)
                }
                if (it.estateDetail.roomLayouts.villaFive?.price != null) {
                    it.estateDetail.roomLayouts.villaFive?.price!!.min = getPrice(it.estateDetail.roomLayouts.villaFive?.price!!.min, currency)!!
                    it.estateDetail.roomLayouts.villaFive?.price!!.max = getPrice(it.estateDetail.roomLayouts.villaFive?.price!!.max, currency)!!
                    if (it.estateDetail.roomLayouts.villaFive?.price?.avg != null)
                        it.estateDetail.roomLayouts.villaFive?.price!!.avg = getPrice(it.estateDetail.roomLayouts.villaFive?.price?.avg, currency)
                }

                if (it.estateDetail.units.isNotNullOrEmpty()) {
                    it.estateDetail.units?.forEach { unit ->
                        unit.price = dec.format(getPrice(strToBigDecimal(unit.price), currency)).toString()
                        unit.priceSq = dec.format(getPrice(strToBigDecimal(unit.priceSq), currency)).toString()
                    }
                }
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

    private fun strToBigDecimal(str: String?): BigDecimal? {
        return str?.replace(" ", "")
            ?.replace(",", ".")
            ?.replace("%", "")
            ?.replace(" ", "")
            ?.nullIfEmpty()
            ?.toBigDecimal()
    }

    override fun update(id: UUID, rq: EstateCollectionUpdateDto) {
        val entity = estateCollectionService.findById(id)
        if (rq.name != null)
            entity.collectionDetail.name = rq.name
        if (rq.comment != null)
            entity.collectionDetail.comment = rq.comment
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
            subject = "Your client liked a property from your selection!",
            message = "Property: ${rq.title}\nSelection: ${rq.collection}\n${rq.url}"
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
            8 -> estateCollectionService.create(
                userId = rq.userId,
                ids = listOf(
                    UUID.fromString("01998445-25de-7242-beac-c00b987b1fbe"),
                    UUID.fromString("0198a285-ed2d-77c2-bd40-f4bcc9f9ce50"),
                    UUID.fromString("0197c6a7-1ab3-7152-9b24-2e30eb9c7cf1"),
                    UUID.fromString("01952e8b-1c90-74a8-acec-5d9b8172b4de"),
                    UUID.fromString("01952e8b-1c8d-7aef-ad95-3c94ecc219fa"),
                    UUID.fromString("01952e8b-1ca5-722a-9e05-8db5625e1631"),
                    UUID.fromString("01952e8b-1c94-75d1-9d17-b26919f3fb51"),
                    UUID.fromString("019ab49d-5075-7475-ab07-a4b85cc0c4c2"),
                    UUID.fromString("019a34a4-4b1e-70fa-a101-c59d3df084bb"),
                    UUID.fromString("01952e8b-1c98-7072-bbc1-0084b629e068"),
                    UUID.fromString("01996238-4102-79a0-8a3e-a3978f38bb95"),
                    UUID.fromString("0199f066-1fd7-7b6b-bd90-23d7f900dfd3"),
                    UUID.fromString("0198a285-ed33-74d3-9df9-20aef4e04cb2"),
                    UUID.fromString("01952e8b-1ca8-72fb-94ea-dc8a2f9df3b4"),
                    UUID.fromString("019a52ff-23c0-79f3-bbe1-f9cc64d781dd"),
                    UUID.fromString("01998445-25e5-7b61-814b-ffe90701bdf2"),
                    UUID.fromString("019a52ff-23c1-794c-aaaa-0f2363294fdb"),
                    UUID.fromString("0198e6d3-2036-7e78-9ce2-b120145f5a76"),
                    UUID.fromString("01952e8b-1c9d-7438-9ba1-3126c32ddf99"),
                    UUID.fromString("0199c756-c713-7af5-adde-a6e853eaa68c"),
                    UUID.fromString("01960bbc-d39f-708f-bdca-35ff1a0a5a76"),
                    UUID.fromString("0197e9ec-1046-78c2-bc2f-9860a4221916"),
                    UUID.fromString("0199c756-c711-77cc-b8ef-da575a4f3206"),
                    UUID.fromString("01998445-25e5-7b61-814b-ffe90701bdf3"),
                    UUID.fromString("0199c756-c710-79f8-86e0-3c15b26d3eeb"),
                    UUID.fromString("01960bbc-d39f-708f-bdca-35ff1a0a5a77"),
                    UUID.fromString("0198a285-ed38-7c97-b4d8-1778c701488e"),
                    UUID.fromString("019a81f6-2bf6-7f33-83e8-87fc68af75b4"),
                    UUID.fromString("01952e8b-1ca0-70a2-aa2e-1d62b6e0eb48"),
                    UUID.fromString("01952e8b-1ca0-70a2-aa2e-1d62b6e0eb49"),
                    UUID.fromString("01952e8b-1c7f-7a5e-883b-b2013ecfdf12"),
                    UUID.fromString("01952e8b-1c7f-7a5e-883b-b2013ecfdf10"),
                    UUID.fromString("01952e8b-1c87-7625-83bb-3fcbd4d20eb2"),
                    UUID.fromString("01952e8b-1c78-723c-b872-d5fc213ae1ab"),
                    UUID.fromString("0199c756-c710-79f8-86e0-3c15b26d3eea"),
                    UUID.fromString("01952e8b-1c87-7625-83bb-3fcbd4d20eb1"),
                    UUID.fromString("01960bbc-d39e-71d9-a45a-6d8e4c4ad1d9"),
                    UUID.fromString("01952e8b-1c9a-7dd0-85b9-cab80f0123ac"),
                    UUID.fromString("01992cf5-08ec-72d4-b2e5-3bc9913fe5e9"),
                    UUID.fromString("01952e8b-1c77-77e8-89f4-dbdb2c335d2d"),
                    UUID.fromString("01952e8b-1c8e-7773-bcc7-f1fa7123fa30"),
                    UUID.fromString("01952e8b-1c8a-7172-a6e9-2f5b1dcf321e"),
                    UUID.fromString("0199c756-c711-77cc-b8ef-da575a4f3200"),
                    UUID.fromString("01952e8b-1c94-75d1-9d17-b26919f3fb50"),
                    UUID.fromString("01952e8b-1c91-777d-a446-1597881a91a3"),
                    UUID.fromString("019a6c3e-2053-7d6f-9e9d-fc6095a206aa"),
                    UUID.fromString("01998445-25e4-7750-8001-f889f0f68b6e"),
                    UUID.fromString("0199c756-c712-7ecc-9855-0029d8c24576"),
                    UUID.fromString("01960bbc-d39e-71d9-a45a-6d8e4c4ad1db"),
                    UUID.fromString("0199f066-1fd7-7b6b-bd90-23d7f900dfd2"),
                    UUID.fromString("019a34a4-4b1e-70fa-a101-c59d3df084be"),
                    UUID.fromString("01952e8b-1ca8-72fb-94ea-dc8a2f9df3b5"),
                    UUID.fromString("01960bbc-d39f-708f-bdca-35ff1a0a5a78"),
                    UUID.fromString("01960bbc-d39d-7034-a858-79b43974baef"),
                    UUID.fromString("01960bbc-d39d-7034-a858-79b43974baf1"),
                    UUID.fromString("0199f066-1fd7-7b6b-bd90-23d7f900dfd9"),
                    UUID.fromString("01998445-25e5-7b61-814b-ffe90701bdf4"),
                    UUID.fromString("01952e8b-1c88-7807-8ff7-3ecb6820ace8"),
                    UUID.fromString("0199c756-c711-77cc-b8ef-da575a4f3204"),
                    UUID.fromString("0198a285-ed29-7a15-851a-75c8553720cd"),
                    UUID.fromString("01952e8b-1c8d-7aef-ad95-3c94ecc219f8"),
                    UUID.fromString("019a90d9-e5e0-7004-acd7-c8fd298124b6"),
                    UUID.fromString("0199c756-c70a-71b7-ad90-060761bcf124"),
                    UUID.fromString("019a7b3b-d9c0-771c-8070-5830531a8d96"),
                    UUID.fromString("019a63e3-6197-7234-8abb-b11dd2413d0d"),
                    UUID.fromString("01952e8b-1c9a-7dd0-85b9-cab80f0123aa"),
                    UUID.fromString("0199c756-c710-79f8-86e0-3c15b26d3eee"),
                    UUID.fromString("01952e8b-1c7a-7422-b9ac-8b091eaeeac6"),
                    UUID.fromString("01952e8b-1c97-781b-b492-b08314b4c384"),
                    UUID.fromString("01952e8b-1ca3-7675-b6e8-de49cf1a8401"),
                    UUID.fromString("0199c756-c712-7ecc-9855-0029d8c24571"),
                    UUID.fromString("01952e8b-1c85-71b8-9446-09226b121b19"),
                    UUID.fromString("0198a285-ed33-74d3-9df9-20aef4e04cb3"),
                    UUID.fromString("0198a285-ed2e-733a-bf02-9d1a82bd3688"),
                    UUID.fromString("01952e8b-1c97-781b-b492-b08314b4c385"),
                    UUID.fromString("0198a285-ed32-7b2b-99d7-c524d29694b7"),
                    UUID.fromString("01952e8b-1c9b-7006-a52c-5e4412e5eb10"),
                    UUID.fromString("01952e8b-1c93-797f-81ab-80204041988a"),
                    UUID.fromString("01952e8b-1c82-772b-b90a-00a393e47909"),
                    UUID.fromString("01960bbc-d39d-7034-a858-79b43974baf7"),
                    UUID.fromString("019a38e5-98ed-7c49-9a0d-fe7af656731c"),
                    UUID.fromString("01952e8b-1c77-77e8-89f4-dbdb2c335d2e"),
                    UUID.fromString("0199dc27-e598-7e24-9de1-2db0212b5403"),
                    UUID.fromString("0199c756-c712-7ecc-9855-0029d8c24575"),
                    UUID.fromString("019a52ff-23c0-79f3-bbe1-f9cc64d781dc"),
                    UUID.fromString("01960bbc-d39d-7034-a858-79b43974baf0"),
                    UUID.fromString("0199c756-c711-77cc-b8ef-da575a4f320c"),
                    UUID.fromString("0199c756-c711-77cc-b8ef-da575a4f320d"),
                    UUID.fromString("0199c756-c712-7ecc-9855-0029d8c2456b"),
                    UUID.fromString("01960bbc-d39f-708f-bdca-35ff1a0a5a73"),
                    UUID.fromString("01952e8b-1c74-7443-9809-bbbfe626e28e"),
                    UUID.fromString("0199c756-c711-77cc-b8ef-da575a4f3205"),
                    UUID.fromString("01960bbc-d39e-71d9-a45a-6d8e4c4ad1d4"),
                    UUID.fromString("0199c756-c711-77cc-b8ef-da575a4f320b"),
                    UUID.fromString("019a81f6-2bfe-77f0-8127-c3ec57ecec86"),
                    UUID.fromString("019ab49d-5074-7e45-9469-e107782e9b6c"),
                    UUID.fromString("01952e8b-1c7f-7a5e-883b-b2013ecfdf11"),
                    UUID.fromString("01996238-4101-778f-945d-b76b22c4fffb")
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

    override fun duplicate(id: UUID) {
        estateCollectionService.duplicate(id)
    }

    override fun activity(rq: ActivityDto) {
        val collection = estateCollectionService.findById(rq.id)
        val user = userService.getUserById(collection.collectionDetail.userId)
        emailService.sendMessage(
            email = user.login,
            subject = "Client opened selection!",
            message = "Selection: ${collection.collectionDetail.name}\n\n${rq.url}"
        )
    }

    private fun getPrice(
        price: BigDecimal?,
        currency: String?
    ): BigDecimal? {
        if (price == null)
            return null
        return currencyService.getValueByCurrency(
            value = price,
            currency = currency ?: "THB"
        )
    }
}

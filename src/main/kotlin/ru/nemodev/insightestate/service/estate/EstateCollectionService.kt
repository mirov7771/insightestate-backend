package ru.nemodev.insightestate.service.estate

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateCollectionCreateDtoRq
import ru.nemodev.insightestate.domen.EstateCollection
import ru.nemodev.insightestate.entity.EstateCollectionDetail
import ru.nemodev.insightestate.entity.EstateCollectionEntity
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.insightestate.repository.EstateCollectionRepository
import ru.nemodev.insightestate.service.UserService
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.ForbiddenLogicalException
import ru.nemodev.platform.core.exception.logic.NotFoundLogicalException
import java.util.*
import kotlin.jvm.optionals.getOrNull

interface EstateCollectionService {
    fun findAll(authBasicToken: String, pageable: Pageable): List<EstateCollection>
    fun create(authBasicToken: String, request: EstateCollectionCreateDtoRq): EstateCollectionEntity
    fun findById(authBasicToken: String, id: UUID): EstateCollectionEntity
    fun addEstateToCollection(authBasicToken: String, id: UUID, estateId: UUID)
    fun deleteEstateFromCollection(authBasicToken: String, id: UUID, estateId: UUID)
    fun deleteById(authBasicToken: String, id: UUID)
    fun findById(id: UUID): EstateCollectionEntity
    fun findEstates(ids: Set<UUID>): List<EstateEntity>
    fun update(collection: EstateCollectionEntity)
}

@Service
class EstateCollectionServiceImpl(
    private val userService: UserService,
    private val estateService: EstateService,
    private val estateCollectionRepository: EstateCollectionRepository
) : EstateCollectionService {

    override fun findAll(authBasicToken: String, pageable: Pageable): List<EstateCollection> {
        val userEntity = userService.getUser(authBasicToken)

        val estateCollections = estateCollectionRepository.findAllByParams(
            userId = userEntity.id.toString(),
            limit = pageable.pageSize,
            offset = pageable.offset
        )

        val estateMap = estateService.findByIds(
            estateCollections.flatMap { it.collectionDetail.estateIds }.toSet()
        ).associateBy { it.id }

        return estateCollections.map { estateCollection ->
            EstateCollection(
                estateCollection = estateCollection,
                estates = estateCollection.collectionDetail.estateIds.mapNotNull { estateMap[it] }
            )
        }
    }

    override fun create(authBasicToken: String, request: EstateCollectionCreateDtoRq): EstateCollectionEntity {
        val userEntity = userService.getUser(authBasicToken)
        val estateEntity = request.estateId?.let { estateService.findById(it) }

        return estateCollectionRepository.save(
            EstateCollectionEntity(
                collectionDetail = EstateCollectionDetail(
                    userId = userEntity.id,
                    name = request.name,
                    estateIds = estateEntity?.let { listOf(it.id) }?.toMutableList() ?: mutableListOf(),
                )
            )
        )
    }

    override fun findById(authBasicToken: String, id: UUID): EstateCollectionEntity {
        val userEntity = userService.getUser(authBasicToken)
        val estateCollection = findById(id = id, userId = userEntity.id)

        return estateCollection
    }

    override fun addEstateToCollection(authBasicToken: String, id: UUID, estateId: UUID) {
        val userEntity = userService.getUser(authBasicToken)
        val estateCollectionEntity = findById(id = id, userId = userEntity.id)
        val estate = estateService.findById(estateId)

        if (estateId !in estateCollectionEntity.collectionDetail.estateIds) {
            estateCollectionEntity.collectionDetail.estateIds.addLast(estate.id)
            estateCollectionRepository.save(estateCollectionEntity)
        }
    }

    override fun deleteEstateFromCollection(authBasicToken: String, id: UUID, estateId: UUID) {
        val userEntity = userService.getUser(authBasicToken)
        val estateCollectionEntity = findById(id = id, userId = userEntity.id)
        val estate = estateService.findById(estateId)

        if (estate.id in estateCollectionEntity.collectionDetail.estateIds) {
            estateCollectionEntity.collectionDetail.estateIds.remove(estate.id)
            estateCollectionRepository.save(estateCollectionEntity)
        }
    }

    override fun deleteById(authBasicToken: String, id: UUID) {
        val userEntity = userService.getUser(authBasicToken)
        val estateCollectionEntity = findById(id = id, userId = userEntity.id)

        estateCollectionRepository.deleteById(estateCollectionEntity.id)
    }

    private fun findById(id: UUID, userId: UUID): EstateCollectionEntity {
        val estateCollectionEntity = findById(id)
        if (estateCollectionEntity.collectionDetail.userId != userId) {
            throw ForbiddenLogicalException(
                errorCode = ErrorCode.createForbidden("User have not permission to estate collection")
            )
        }

        return estateCollectionEntity
    }

    override fun findById(id: UUID): EstateCollectionEntity {
        return estateCollectionRepository.findById(id).getOrNull()
            ?: throw NotFoundLogicalException(
                errorCode = ErrorCode.createNotFound("Estate collection not found")
            )
    }

    override fun findEstates(ids: Set<UUID>): List<EstateEntity> {
        val estates = estateService.findByIds(ids)
        return estates
    }

    override fun update(collection: EstateCollectionEntity) {
        estateCollectionRepository.save(collection.apply { isNew = false })
    }
}

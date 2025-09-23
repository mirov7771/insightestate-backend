package ru.nemodev.insightestate.service.estate

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateCollectionCreateDtoRq
import ru.nemodev.insightestate.domen.EstateCollection
import ru.nemodev.insightestate.entity.EstateCollectionDetail
import ru.nemodev.insightestate.entity.EstateCollectionEntity
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.insightestate.entity.UnitEstateLink
import ru.nemodev.insightestate.repository.EstateCollectionRepository
import ru.nemodev.insightestate.repository.UnitRepository
import ru.nemodev.insightestate.service.UserService
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.ForbiddenLogicalException
import ru.nemodev.platform.core.exception.logic.NotFoundLogicalException
import ru.nemodev.platform.core.extensions.isNotNullOrEmpty
import java.util.*
import kotlin.jvm.optionals.getOrNull

interface EstateCollectionService {
    fun findAll(authBasicToken: String, pageable: Pageable): List<EstateCollection>
    fun create(authBasicToken: String, request: EstateCollectionCreateDtoRq): EstateCollectionEntity
    fun create(userId: UUID, ids: List<UUID>, template: String): EstateCollectionEntity
    fun findById(authBasicToken: String, id: UUID): EstateCollectionEntity
    fun addEstateToCollection(authBasicToken: String, id: UUID, estateId: UUID, unitId: UUID?)
    fun deleteEstateFromCollection(authBasicToken: String, id: UUID, estateId: UUID)
    fun deleteById(authBasicToken: String, id: UUID)
    fun findById(id: UUID): EstateCollectionEntity
    fun findEstates(ids: Set<UUID>): List<EstateEntity>
    fun findEstatesWithUnites(ids: List<UnitEstateLink>): List<EstateEntity>
    fun update(collection: EstateCollectionEntity)
}

@Service
class EstateCollectionServiceImpl(
    private val userService: UserService,
    private val estateService: EstateService,
    private val estateCollectionRepository: EstateCollectionRepository,
    private val unitRepository: UnitRepository,
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
            val estateList = estateCollection.collectionDetail.estateIds.mapNotNull { estateMap[it] }.toMutableList()

            if (estateCollection.collectionDetail.unitIds.isNotNullOrEmpty()) {
                val uniqueIds = estateCollection.collectionDetail.unitIds?.map { it.estateId }?.distinct()
                val estates = estateService.findByIds(uniqueIds!!.toSet())
                if (estates.isNotEmpty()) {
                    estates.forEach {
                        val uIds = estateCollection.collectionDetail.unitIds!!.filter { unit -> unit.estateId == it.id }.map { it.unitId }.distinct().toSet()
                        val units = unitRepository.findAllById(uIds)
                        if (units.isNotEmpty()) {
                            it.estateDetail.units = units
                            estateList.addLast(it)
                        }
                    }
                }
            }

            EstateCollection(
                estateCollection = estateCollection,
                estates = estateList
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

    override fun create(userId: UUID, ids: List<UUID>, template: String): EstateCollectionEntity {
        val userEntity = userService.getUserById(userId)
        return estateCollectionRepository.save(
            EstateCollectionEntity(
                collectionDetail = EstateCollectionDetail(
                    userId = userEntity.id,
                    name = template,
                    estateIds = ids.toMutableList(),
                )
            )
        )
    }

    override fun findById(authBasicToken: String, id: UUID): EstateCollectionEntity {
        val userEntity = userService.getUser(authBasicToken)
        val estateCollection = findById(id = id, userId = userEntity.id)

        return estateCollection
    }

    override fun addEstateToCollection(authBasicToken: String, id: UUID, estateId: UUID, unitId: UUID?) {
        val userEntity = userService.getUser(authBasicToken)
        val estateCollectionEntity = findById(id = id, userId = userEntity.id)
        val estate = estateService.findById(estateId)

        val unitIds = estateCollectionEntity.collectionDetail.unitIds ?: emptyList()
        if (unitId != null && !unitIds.any { it.unitId == unitId }) {
            if (unitIds.isEmpty()) {
                estateCollectionEntity.collectionDetail.unitIds = mutableListOf(
                    UnitEstateLink(
                        estateId = estateId,
                        unitId = unitId
                    )
                )
            } else {
                estateCollectionEntity.collectionDetail.unitIds?.addLast(
                    UnitEstateLink(
                        estateId = estateId,
                        unitId = unitId
                    )
                )
            }
            val estateIds = estateCollectionEntity.collectionDetail.estateIds
            if (estateIds.isNotNullOrEmpty()) {
                if (estateId in estateIds) {
                    estateCollectionEntity.collectionDetail.estateIds = estateIds.filter { it != estateId }.toMutableList()
                }
            }
            estateCollectionRepository.save(estateCollectionEntity)
        } else if (estateId !in estateCollectionEntity.collectionDetail.estateIds && !unitIds.any { it.estateId == estateId }) {
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

        if (estateCollectionEntity.collectionDetail.unitIds.isNotNullOrEmpty()) {
            val unitIds = estateCollectionEntity.collectionDetail.unitIds?.filter { unit -> unit.estateId != estateId }
            estateCollectionEntity.collectionDetail.unitIds = unitIds?.toMutableList()
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

    override fun findEstatesWithUnites(ids: List<UnitEstateLink>): List<EstateEntity> {
        val uniqueIds = ids.map { it.estateId }.distinct()
        val estates = estateService.findByIds(uniqueIds.toSet())
        if (estates.isNotEmpty()) {
            estates.forEach {
                val uIds = ids.filter { unit -> unit.estateId == it.id }.map { it.unitId }.distinct().toSet()
                val units = unitRepository.findAllById(uIds)
                if (units.isNotEmpty()) {
                    it.estateDetail.units = units
                }
            }
        }
        return estates
    }

    override fun update(collection: EstateCollectionEntity) {
        estateCollectionRepository.save(collection.apply { isNew = false })
    }
}

package ru.nemodev.insightestate.repository

import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.LikesEntity
import java.util.*

@Repository
interface LikesRepository: ListCrudRepository<LikesEntity, UUID> {
    fun findByCollectionIdAndEstateId(
        collectionId: UUID,
        estateId: UUID
    ): List<LikesEntity>
}

package ru.nemodev.insightestate.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.EstateCollectionEntity
import java.util.*

@Repository
interface EstateCollectionRepository : ListCrudRepository<EstateCollectionEntity, UUID> {

    @Query("""
        select * from estate_collections 
        where collection_detail ->> 'userId' = :userId
        order by created_at desc
        limit :limit offset :offset
    """)
    fun findAllByParams(
        userId: String,
        limit : Int,
        offset : Long,
    ): List<EstateCollectionEntity>
}

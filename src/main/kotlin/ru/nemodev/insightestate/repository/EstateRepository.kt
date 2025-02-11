package ru.nemodev.insightestate.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.EstateEntity
import java.util.*

@Repository
interface EstateRepository: ListCrudRepository<EstateEntity, UUID> {
    @Query("""
        select *
          from estate  
       where (:priceStart is null or (estate_detail ->> 'priceStart' ->> :priceStart))
         and (:priceEnd is null or (estate_detail ->> 'priceEnd' ->> :priceEnd))
         and (:type is null or (estate_detail ->> 'type' ->> :type))
        order by created_at desc
        limit :limit offset :offset
    """)
    fun findByParams(
        priceStart: Long?,
        priceEnd: Long?,
        type: String?,
        offset: Long,
        limit: Int
    ): List<EstateEntity>
}

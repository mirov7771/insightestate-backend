package ru.nemodev.insightestate.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.UnitEntity
import java.util.*

@Repository
interface UnitRepository: ListCrudRepository<UnitEntity, UUID> {
    @Query("""
        select *
           from unit
          where code like :projectId
    """)
    fun findByProjectId(projectId: String): List<UnitEntity>

    @Query(
        """
        select * from unit
        where code in (:codes)
        """
    )
    fun findAllByCodes(
        codes: List<String>,
    ): List<UnitEntity>

    @Query("""
        select count(*) from estate_collections 
        where collection_detail ->> 'userId' = :userId
    """)
    fun findAllCollections(userId: String): Int
}

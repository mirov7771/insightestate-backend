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
       where (:priceStart is null or (cast(estate_detail ->> 'priceStart' as numeric) > :priceStart))
         and (:priceEnd is null or (cast(estate_detail ->> 'priceEnd' as numeric) <= :priceEnd))
         and (:type is null or (estate_detail ->> 'type' = :type))
         and (:isStudio is null or (estate_detail ->> 'isStudio' = :isStudio))
         and (:isOneRoom is null or (estate_detail ->> 'isOneRoom' = :isOneRoom))
         and (:isTwoRoom is null or (estate_detail ->> 'isTwoRoom' = :isTwoRoom))
         and (:isThreeRoom is null or (estate_detail ->> 'isThreeRoom' = :isThreeRoom))
         and (:isFourRoom is null or (estate_detail ->> 'isFourRoom' = :isFourRoom))
         and (:isFiveRoom is null or (estate_detail ->> 'isFiveRoom' = :isFiveRoom))
         and (:attachmentSecurity is null or (cast(estate.estate_detail ->> 'attachmentSecurity' as numeric) > :attachmentSecurity))
         and (:investmentPotential is null or (cast(estate.estate_detail ->> 'investmentPotential' as numeric) > :investmentPotential))
         and (:locationOfTheObject is null or (cast(estate.estate_detail ->> 'locationOfTheObject' as numeric) > :locationOfTheObject))
         and (:comfortOfLife is null or (cast(estate.estate_detail ->> 'comfortOfLife' as numeric) > :comfortOfLife))
        order by created_at desc
        limit :limit offset :offset
    """)
    fun findByParams(
        isStudio: String?,
        isOneRoom: String?,
        isTwoRoom: String?,
        isThreeRoom: String?,
        isFourRoom: String?,
        isFiveRoom: String?,
        priceStart: Long?,
        priceEnd: Long?,
        type: String?,
        attachmentSecurity: Int?,
        investmentPotential: Int?,
        locationOfTheObject: Int?,
        comfortOfLife: Int?,
        offset: Long,
        limit: Int
    ): List<EstateEntity>
}

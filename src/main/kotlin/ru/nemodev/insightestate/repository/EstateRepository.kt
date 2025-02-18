package ru.nemodev.insightestate.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.EstateEntity
import java.math.BigDecimal
import java.util.*

@Repository
interface EstateRepository: ListCrudRepository<EstateEntity, UUID> {

    @Query("""
        select * from estate  
        where (:types::text[] is null or (estate_detail ->> 'type' = any(:types)))
            and (:buildEndYears::text[] is null or (substring(estate_detail ->> 'buildEndDate', 1, 4) = any(:buildEndYears)))
            and ((:isStudioRoom is null or (:isStudioRoom and (estate_detail -> 'roomLayouts' -> 'studio' is not null)))
                or (:isOneRoom is null or (:isOneRoom and (estate_detail -> 'roomLayouts' -> 'one' is not null)))
                or (:isTwoRoom is null or (:isTwoRoom and ((estate_detail -> 'roomLayouts' -> 'two' is not null) or (estate_detail -> 'roomLayouts' -> 'villaTwo' is not null))))
                or (:isFreeRoom is null or (:isFreeRoom and ((estate_detail -> 'roomLayouts' -> 'free' is not null) or (estate_detail -> 'roomLayouts' -> 'villaFree' is not null))))
                or (:isFourRoom is null or (:isFourRoom and ((estate_detail -> 'roomLayouts' -> 'four' is not null) or (estate_detail -> 'roomLayouts' -> 'five' is not null) or (estate_detail -> 'roomLayouts' -> 'villaFour' is not null) or (estate_detail -> 'roomLayouts' -> 'villaFive' is not null))))
            )
            and (:minPrice is null or ((estate_detail -> 'price' ->> 'min')::numeric > :minPrice)
                and :maxPrice is null or ((estate_detail -> 'price' ->> 'max')::numeric <= :maxPrice)
            )
            and ((:gradeInvestmentSecurity is null or ((estate_detail -> 'grade' ->> 'investmentSecurity')::numeric >= :gradeInvestmentSecurity))
                and (:gradeInvestmentPotential is null or ((estate_detail -> 'grade' ->> 'investmentPotential')::numeric >= :gradeInvestmentPotential))
                and (:gradeProjectLocation is null or ((estate_detail -> 'grade' ->> 'projectLocation')::numeric >= :gradeProjectLocation))
                and (:gradeComfortOfLife is null or ((estate_detail -> 'grade' ->> 'comfortOfLife')::numeric >= :gradeComfortOfLife))
            )
        order by created_at desc
        limit :limit offset :offset
    """)
    fun findByParams(
        types: Array<String>?,
        buildEndYears: Array<String>?,
        isStudioRoom: Boolean?,
        isOneRoom: Boolean?,
        isTwoRoom: Boolean?,
        isFreeRoom: Boolean?,
        isFourRoom: Boolean?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        gradeInvestmentSecurity: BigDecimal?,
        gradeInvestmentPotential: BigDecimal?,
        gradeProjectLocation: BigDecimal?,
        gradeComfortOfLife: BigDecimal?,
        offset: Long,
        limit: Int
    ): List<EstateEntity>
}

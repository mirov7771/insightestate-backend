package ru.nemodev.insightestate.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.EstateEntity
import java.math.BigDecimal
import java.util.*

@Repository
interface EstateRepository: ListCrudRepository<EstateEntity, UUID> {

    // TODO в целом т.к это справочник и записей мало можно вытаскивать просто весь список и всю фильтрацию делать в коде? =)
    // т.к запрос сложный для понимания в целом но простой для исполнения =)
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
                and :maxPrice is null or ((estate_detail -> 'price' ->> 'min')::numeric <= :maxPrice)
            )
            and ((:gradeInvestmentSecurity is null or ((estate_detail -> 'grade' ->> 'investmentSecurity')::numeric >= :gradeInvestmentSecurity))
                and (:gradeInvestmentPotential is null or ((estate_detail -> 'grade' ->> 'investmentPotential')::numeric >= :gradeInvestmentPotential))
                and (:gradeProjectLocation is null or ((estate_detail -> 'grade' ->> 'projectLocation')::numeric >= :gradeProjectLocation))
                and (:gradeComfortOfLife is null or ((estate_detail -> 'grade' ->> 'comfortOfLife')::numeric >= :gradeComfortOfLife))
            )
            and ((:maxBeachWalkTravelTimeOne is null or ((estate_detail -> 'infrastructure' -> 'beachTime' ->> 'walk')::numeric) <= :maxBeachWalkTravelTimeOne)
                and (:minBeachWalkTravelTimeTwo is null or ((:minBeachWalkTravelTimeTwo <= (estate_detail -> 'infrastructure' -> 'beachTime' ->> 'walk')::numeric) and ((estate_detail -> 'infrastructure' -> 'beachTime' ->> 'walk')::numeric <= :maxBeachWalkTravelTimeTwo)))
                and (:minBeachWalkTravelTimeFree is null or ((:minBeachWalkTravelTimeFree <= (estate_detail -> 'infrastructure' -> 'beachTime' ->> 'walk')::numeric) and ((estate_detail -> 'infrastructure' -> 'beachTime' ->> 'walk')::numeric  <= :minBeachWalkTravelTimeFree)))
                and (:maxBeachCarTravelTimeOne is null or ((estate_detail -> 'infrastructure' -> 'beachTime' ->> 'car')::numeric) <= :maxBeachCarTravelTimeOne)
                and (:minBeachCarTravelTimeTwo is null or ((:minBeachCarTravelTimeTwo <= (estate_detail -> 'infrastructure' -> 'beachTime' ->> 'car')::numeric) and ((estate_detail -> 'infrastructure' -> 'beachTime' ->> 'car')::numeric <= :maxBeachCarTravelTimeTwo)))
                and (:minBeachCarTravelTimeFree is null or ((:minBeachCarTravelTimeFree <= (estate_detail -> 'infrastructure' -> 'beachTime' ->> 'car')::numeric) and ((estate_detail -> 'infrastructure' -> 'beachTime' ->> 'car')::numeric <= :maxBeachCarTravelTimeFree)))
            )
            and ((:maxAirportCarTravelTimeOne is null or ((estate_detail -> 'infrastructure' -> 'airportTime' ->> 'car')::numeric) <= :maxAirportCarTravelTimeOne)
                and (:minAirportCarTravelTimeTwo is null or ((:minAirportCarTravelTimeTwo <= (estate_detail -> 'infrastructure' -> 'airportTime' ->> 'car')::numeric) and ((estate_detail -> 'infrastructure' -> 'airportTime' ->> 'car')::numeric <= :maxAirportCarTravelTimeTwo)))
                and (:maxAirportCarTravelTimeFree is null or ((estate_detail -> 'infrastructure' -> 'airportTime' ->> 'car')::numeric) > :maxAirportCarTravelTimeFree)
            )
            and (:parking is null or (:parking and ((estate_detail -> 'options' ->> 'parkingSize')::numeric > 0)) or (:parking = false and ((estate_detail -> 'options' -> 'parkingSize' is null))))
            and (:managementCompanyEnabled is null or (:managementCompanyEnabled and ((estate_detail -> 'managementCompany' ->> 'enabled')::bool)) or (not :managementCompanyEnabled and not ((estate_detail -> 'managementCompany' ->> 'enabled')::bool)))
            and (:beachName::text[] is null or (estate_detail -> 'location' ->> 'beach' = any(:beachName)))
            and (:city::text[] is null or (estate_detail -> 'location' ->> 'city' = any(:city)))
            and ((estate_detail ->> 'canShow')::boolean = true)
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

        maxBeachWalkTravelTimeOne: Int?,
        minBeachWalkTravelTimeTwo: Int?,
        maxBeachWalkTravelTimeTwo: Int?,
        minBeachWalkTravelTimeFree: Int?,
        maxBeachWalkTravelTimeFree: Int?,

        maxBeachCarTravelTimeOne: Int?,
        minBeachCarTravelTimeTwo: Int?,
        maxBeachCarTravelTimeTwo: Int?,
        minBeachCarTravelTimeFree: Int?,
        maxBeachCarTravelTimeFree: Int?,

        maxAirportCarTravelTimeOne: Int?,
        minAirportCarTravelTimeTwo: Int?,
        maxAirportCarTravelTimeTwo: Int?,
        maxAirportCarTravelTimeFree: Int?,

        parking: Boolean?,
        managementCompanyEnabled: Boolean?,

        beachName: Array<String>?,

        city: Array<String>?,

        offset: Long,
        limit: Int
    ): List<EstateEntity>

    @Query("""
        select count(*) from estate
        where (estate_detail ->> 'canShow')::boolean = true
    """)
    fun findAllForShowCount(): Int?

    @Query("""
        select * from estate
        where (estate_detail ->> 'canShow')::boolean = true
        order by random()  
        limit 250
    """)
    fun findRandom(): List<EstateEntity>
}

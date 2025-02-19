package ru.nemodev.insightestate.service.estate

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDetailDtoRs
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDtoRs
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.insightestate.repository.EstateRepository
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
import java.math.BigDecimal
import java.util.*

interface EstateService {

    fun findAll(
        types: Set<EstateType>?,
        buildEndYears: Set<Int>?,
        rooms: Set<String>?,
        price: String?,
        grades: Set<String>?,
        beachTravelTimes: Set<String>?,
        airportTravelTimes: Set<String>?,
        parking: Boolean?,
        pageable: Pageable
    ): PageDtoRs<EstateDtoRs>

    fun findById(
        id: UUID
    ): EstateDetailDtoRs
}

@Service
class EstateServiceImpl (
    private val repository: EstateRepository,
) : EstateService {

    override fun findAll(
        types: Set<EstateType>?,
        buildEndYears: Set<Int>?,
        rooms: Set<String>?,
        price: String?,
        grades: Set<String>?,
        beachTravelTimes: Set<String>?,
        airportTravelTimes: Set<String>?,
        parking: Boolean?,
        pageable: Pageable
    ): PageDtoRs<EstateDtoRs> {

        val minPrice = when (price) {
            "1" -> BigDecimal.ZERO
            "2" -> BigDecimal.valueOf(100_000)
            "3" -> BigDecimal.valueOf(200_000)
            "4" -> BigDecimal.valueOf(500_000)
            "5" -> BigDecimal.valueOf(1_000_000)
            else -> null
        }
        val maxPrice = when (price) {
            "1" -> BigDecimal.valueOf(100_000)
            "2" -> BigDecimal.valueOf(200_000)
            "3" -> BigDecimal.valueOf(500_000)
            "4" -> BigDecimal.valueOf(1_000_000)
            "5" -> BigDecimal.valueOf(100_000_000_000) // =)
            else -> null
        }

        val estateList = repository.findByParams(
            types = types?.map { it.name }?.toTypedArray(),
            buildEndYears = buildEndYears?.map { it.toString() }?.toTypedArray(),

            isStudioRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("0"),
            isOneRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("1"),
            isTwoRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("2"),
            isFreeRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("3"),
            isFourRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("4"),

            minPrice = minPrice,
            maxPrice = maxPrice,

            gradeInvestmentSecurity = if (grades.isNullOrEmpty()) null else if (grades.contains("1")) BigDecimal.valueOf(9) else null,
            gradeInvestmentPotential = if (grades.isNullOrEmpty()) null else if (grades.contains("2")) BigDecimal.valueOf(9) else null,
            gradeProjectLocation = if (grades.isNullOrEmpty()) null else if (grades.contains("3")) BigDecimal.valueOf(9) else null,
            gradeComfortOfLife = if (grades.isNullOrEmpty()) null else if (grades.contains("4")) BigDecimal.valueOf(9) else null,

            maxBeachWalkTravelTimeOne = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("1")) 5 else null,
            minBeachWalkTravelTimeTwo = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("2")) 6 else null,
            maxBeachWalkTravelTimeTwo = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("2")) 10 else null,
            minBeachWalkTravelTimeFree = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("3")) 11 else null,
            maxBeachWalkTravelTimeFree = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("3")) 30 else null,

            maxBeachCarTravelTimeOne = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("11")) 5 else null,
            minBeachCarTravelTimeTwo = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("12")) 6 else null,
            maxBeachCarTravelTimeTwo = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("12")) 10 else null,
            minBeachCarTravelTimeFree = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("13")) 11 else null,
            maxBeachCarTravelTimeFree = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("13")) 30 else null,

            maxAirportCarTravelTimeOne = if (airportTravelTimes.isNullOrEmpty()) null else if (airportTravelTimes.contains("1")) 30 else null,
            minAirportCarTravelTimeTwo = if (airportTravelTimes.isNullOrEmpty()) null else if (airportTravelTimes.contains("2")) 31 else null,
            maxAirportCarTravelTimeTwo = if (airportTravelTimes.isNullOrEmpty()) null else if (airportTravelTimes.contains("2")) 60 else null,
            maxAirportCarTravelTimeFree = if (airportTravelTimes.isNullOrEmpty()) null else if (airportTravelTimes.contains("3")) 61 else null,

            parking = parking,

            limit = pageable.pageSize,
            offset = pageable.offset
        )

        return PageDtoRs(
            items = estateList.map {
                EstateDtoRs(
                    id = it.id,
                    projectId = it.estateDetail.projectId,
                    name = it.estateDetail.name,
                )
           },
            pageSize = estateList.size,
            pageNumber = pageable.pageNumber,
            hasMore = estateList.size >= pageable.pageSize
        )
    }

    override fun findById(
        id: UUID
    ): EstateDetailDtoRs {
        TODO()
//        val dao = repository.findById(id).orElseThrow {
//            NotFoundLogicalException(errorCode = ErrorCode.createNotFound("Объект не найден"))
//        }
//        return EstateDetailDtoRs(
//            id = dao.id,
//            rate = dao.estateDetail.rate,
//            name = dao.estateDetail.name,
//            profitAmount = dao.estateDetail.profitAmount,
//            profitTerm = dao.estateDetail.profitTerm,
//            images = dao.estateDetail.images ?: emptyList(),
//            type = dao.estateDetail.type,
//            square = dao.estateDetail.square,
//            attachmentSecurity = dao.estateDetail.attachmentSecurity,
//            investmentPotential = dao.estateDetail.investmentPotential,
//            locationOfTheObject = dao.estateDetail.locationOfTheObject,
//            comfortOfLife = dao.estateDetail.comfortOfLife,
//            deliveryDate = dao.estateDetail.deliveryDate,
//            floors = dao.estateDetail.floors,
//            apartments = dao.estateDetail.apartments,
//            beach = dao.estateDetail.beach,
//            airport = dao.estateDetail.airport,
//            parking = dao.estateDetail.parking,
//            developer = dao.estateDetail.developerName,
//            level = dao.estateDetail.level,
//            mall = dao.estateDetail.mall,
//            childRoom = dao.estateDetail.childRoom,
//            coWorking = dao.estateDetail.coWorking,
//            gym = dao.estateDetail.gym,
//            rentalIncome = dao.estateDetail.rentalIncome,
//            roi = dao.estateDetail.roi,
//            irr = dao.estateDetail.irr,
//            projectImage = dao.estateDetail.projectImage,
//            district = dao.estateDetail.district,
//            geoPosition = dao.estateDetail.geoPosition,
//        )
    }
}

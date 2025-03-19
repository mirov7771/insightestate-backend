package ru.nemodev.insightestate.service.estate

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.insightestate.repository.EstateRepository
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.NotFoundLogicalException
import java.math.BigDecimal
import java.util.*
import kotlin.jvm.optionals.getOrNull

interface EstateService {

    fun findAll(): List<EstateEntity>
    fun findAll(
        types: Set<EstateType>?,
        buildEndYears: Set<Int>?,
        rooms: Set<String>?,
        price: String?,
        grades: Set<String>?,
        beachTravelTimes: Set<String>?,
        airportTravelTimes: Set<String>?,
        parking: Boolean?,
        managementCompanyEnabled: Boolean?,
        beachName: String?,
        pageable: Pageable
    ): List<EstateEntity>

    fun findById(
        id: UUID
    ): EstateEntity

    fun saveAll(estates: List<EstateEntity>)
    fun findByIds(ids: List<UUID>): List<EstateEntity>
    fun findPages(pageCount: Int): Int
}

@Service
class EstateServiceImpl(
    private val repository: EstateRepository,
) : EstateService {

    override fun findAll(): List<EstateEntity> {
        return repository.findAll()
    }

    override fun findAll(
        types: Set<EstateType>?,
        buildEndYears: Set<Int>?,
        rooms: Set<String>?,
        price: String?,
        grades: Set<String>?,
        beachTravelTimes: Set<String>?,
        airportTravelTimes: Set<String>?,
        parking: Boolean?,
        managementCompanyEnabled: Boolean?,
        beachName: String?,
        pageable: Pageable
    ): List<EstateEntity> {

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

        val estates = repository.findByParams(
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
            managementCompanyEnabled = managementCompanyEnabled,

            beachName = beachName,

            limit = pageable.pageSize,
            offset = pageable.offset
        )

        return estates
    }

    override fun findById(id: UUID): EstateEntity {
        val estateEntity = repository.findById(id).getOrNull()
            ?: throw NotFoundLogicalException(errorCode = ErrorCode.createNotFound("Project object not found"))

        return estateEntity
    }

    override fun saveAll(estates: List<EstateEntity>) {
        repository.saveAll(estates)
    }

    override fun findByIds(ids: List<UUID>): List<EstateEntity> {
        return repository.findAllById(ids)
    }

    override fun findPages(pageCount: Int): Int {
        val count = repository.findAllEstate() ?: return 0
        return count / pageCount
    }
}

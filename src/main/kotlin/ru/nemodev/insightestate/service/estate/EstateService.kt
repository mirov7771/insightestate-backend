package ru.nemodev.insightestate.service.estate

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.insightestate.integration.ai.AiIntegration
import ru.nemodev.insightestate.repository.EstateRepository
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.NotFoundLogicalException
import ru.nemodev.platform.core.extensions.isNotNullOrEmpty
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
        city: String?,
        pageable: Pageable
    ): List<EstateEntity>

    fun findById(
        id: UUID
    ): EstateEntity

    fun saveAll(estates: List<EstateEntity>)
    fun findByIds(ids: List<UUID>): List<EstateEntity>
    fun findPages(pageCount: Int): Int
    fun aiRequest(rq: String): List<EstateEntity>
}

@Service
class EstateServiceImpl(
    private val repository: EstateRepository,
    private val aiIntegration: AiIntegration
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
        city: String?,
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

            city = city,

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
        val count = repository.findAllForShowCount() ?: return 0
        return count / pageCount
    }

    override fun aiRequest(rq: String): List<EstateEntity> {
        val rs = aiIntegration.generate(rq = rq)
        if (rs == null) {
            println("Empty list")
            return repository.findRandom()
        }
        val type = if (rs.type != null) arrayOf(rs.type) else null
        val rooms = rs.rooms
        val isStudioRoom = if (rooms.isNotNullOrEmpty() && rooms == "0") true else null
        val isOneRoom = if (rooms.isNotNullOrEmpty() && rooms == "1") true else null
        val isTwoRoom = if (rooms.isNotNullOrEmpty() && rooms == "2") true else null
        val isFreeRoom = if (rooms.isNotNullOrEmpty() && rooms == "3") true else null
        val isFourRoom = if (rooms.isNotNullOrEmpty() && rooms == "4") true else null
        val buildEndYears = if (rs.buildEndYears != null) arrayOf(rs.buildEndYears) else null

        var minPrice = try {
            (rs.priceFrom ?: "0").toBigDecimal()
        } catch (_: Exception) {
            BigDecimal.ZERO
        }
        var maxPrice =  try {
            (rs.priceTo ?: "1000000000000").toBigDecimal()
        } catch (_: Exception) {
            BigDecimal("1000000000000")
        }

        val currency = rs.currency ?: "THB"
        if (currency === "RUB") {
            minPrice = minPrice.multiply(BigDecimal(0.3))
            maxPrice = maxPrice.multiply(BigDecimal(0.3))
        }
        if (currency === "USD") {
            minPrice = minPrice.multiply(BigDecimal(34))
            maxPrice = maxPrice.multiply(BigDecimal(34))
        }

        val beachWalkTime = try {
            (rs.beachTravelTimesWalk ?: "0").toInt()
        } catch (_: Exception) {
            0
        }

        val beachCarTime = try {
            (rs.beachTravelTimesCar ?: "0").toInt()
        } catch (_: Exception) {
            0
        }

        val airportCarTime = try {
            (rs.airportTravelTimes ?: "0").toInt()
        } catch (_: Exception) {
            0
        }

        var maxBeachWalkTravelTimeOne: Int? = null
        var minBeachWalkTravelTimeTwo: Int? = null
        var maxBeachWalkTravelTimeTwo: Int? = null
        var minBeachWalkTravelTimeFree: Int? = null
        var maxBeachWalkTravelTimeFree: Int? = null

        if (beachWalkTime < 6) {
            maxBeachWalkTravelTimeOne = 5
        } else if (beachWalkTime <= 10) {
            minBeachWalkTravelTimeTwo = 6
            maxBeachWalkTravelTimeTwo = 10
        } else if (beachWalkTime <= 30) {
            minBeachWalkTravelTimeFree = 11
            maxBeachWalkTravelTimeFree = 30
        }

        var maxBeachCarTravelTimeOne: Int? = null
        var minBeachCarTravelTimeTwo: Int? = null
        var maxBeachCarTravelTimeTwo: Int? = null
        var minBeachCarTravelTimeFree: Int? = null
        var maxBeachCarTravelTimeFree: Int? = null

        if (beachCarTime < 6) {
            maxBeachCarTravelTimeOne = 5
        } else if (beachCarTime <= 10) {
            minBeachCarTravelTimeTwo = 6
            maxBeachCarTravelTimeTwo = 10
        } else if (beachCarTime <= 30) {
            minBeachCarTravelTimeFree = 11
            maxBeachCarTravelTimeFree = 30
        }

        var maxAirportCarTravelTimeOne: Int? = null
        var minAirportCarTravelTimeTwo: Int? = null
        var maxAirportCarTravelTimeTwo: Int? = null
        var maxAirportCarTravelTimeFree: Int? = null
        if (airportCarTime <= 30) {
            maxAirportCarTravelTimeOne = 30
        } else if (airportCarTime <= 60) {
            minAirportCarTravelTimeTwo = 31
            maxAirportCarTravelTimeTwo = 60
        } else if (airportCarTime <= 61) {
            maxAirportCarTravelTimeFree = 61
        }

        if (minPrice == maxPrice) {
            minPrice = BigDecimal.ZERO
        }

        val parking =  if (rs.parking != null && rs.parking.equals("true", ignoreCase = true)) true else null

        var list = repository.findByParams(
            types = type,
            buildEndYears = buildEndYears,
            isStudioRoom = isStudioRoom,
            isOneRoom = isOneRoom,
            isTwoRoom = isTwoRoom,
            isFreeRoom = isFreeRoom,
            isFourRoom = isFourRoom,
            minPrice = minPrice,
            maxPrice = maxPrice,
            gradeInvestmentSecurity = null,
            gradeInvestmentPotential = null,
            gradeProjectLocation = null,
            gradeComfortOfLife = null,
            maxBeachWalkTravelTimeOne = null,
            minBeachWalkTravelTimeTwo = null,
            maxBeachWalkTravelTimeTwo = null,
            minBeachWalkTravelTimeFree = null,
            maxBeachWalkTravelTimeFree = null,
            maxBeachCarTravelTimeOne = null,
            minBeachCarTravelTimeTwo = null,
            maxBeachCarTravelTimeTwo = null,
            minBeachCarTravelTimeFree = null,
            maxBeachCarTravelTimeFree = null,
            maxAirportCarTravelTimeOne = null,
            minAirportCarTravelTimeTwo = null,
            maxAirportCarTravelTimeTwo = null,
            maxAirportCarTravelTimeFree = null,
            parking = parking,
            managementCompanyEnabled = null,
            beachName = null,
            city = null,
            offset = 0,
            limit = 25,
        )
        if (list.isEmpty())
            list = repository.findRandom()
        return list
    }
}

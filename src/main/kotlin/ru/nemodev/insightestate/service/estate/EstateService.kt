package ru.nemodev.insightestate.service.estate

import jakarta.annotation.PostConstruct
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.entity.AiRequestEntity
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.insightestate.integration.ai.AiIntegration
import ru.nemodev.insightestate.repository.AiRequestRepository
import ru.nemodev.insightestate.repository.EstateCollectionRepository
import ru.nemodev.insightestate.repository.EstateRepository
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.NotFoundLogicalException
import ru.nemodev.platform.core.extensions.isNotNullOrEmpty
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
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
        beachName: Set<String>?,
        city: Set<String>?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        pageable: Pageable,
        userId: UUID?,
    ): List<EstateEntity>

    fun findById(
        id: UUID
    ): EstateEntity

    fun saveAll(estates: List<EstateEntity>)
    fun findByIds(ids: Set<UUID>): List<EstateEntity>
    fun findPages(
        pageCount: Int,
        allCount: Int
    ): Int
    fun aiRequest(rq: String): List<EstateEntity>
    fun findCount(
        types: Set<EstateType>?,
        buildEndYears: Set<Int>?,
        rooms: Set<String>?,
        price: String?,
        grades: Set<String>?,
        beachTravelTimes: Set<String>?,
        airportTravelTimes: Set<String>?,
        parking: Boolean?,
        managementCompanyEnabled: Boolean?,
        beachName: Set<String>?,
        city: Set<String>?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
    ): Int

    fun createXml(): String
}

@Service
class EstateServiceImpl(
    private val repository: EstateRepository,
    private val aiIntegration: AiIntegration,
    private val aiRequestRepository: AiRequestRepository,
    private val collectionRepository: EstateCollectionRepository
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
        beachName: Set<String>?,
        city: Set<String>?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        pageable: Pageable,
        userId: UUID?
    ): List<EstateEntity> {

        var minTotalPrice =  minPrice ?:
            when (price) {
            "1" -> BigDecimal.ZERO
            "2" -> BigDecimal.valueOf(100_000)
            "3" -> BigDecimal.valueOf(200_000)
            "4" -> BigDecimal.valueOf(500_000)
            "5" -> BigDecimal.valueOf(1_000_000)
            else -> null
        }
        var maxTotalPrice = maxPrice ?: when (price) {
            "1" -> BigDecimal.valueOf(100_000)
            "2" -> BigDecimal.valueOf(200_000)
            "3" -> BigDecimal.valueOf(500_000)
            "4" -> BigDecimal.valueOf(1_000_000)
            "5" -> BigDecimal.valueOf(100_000_000_000) // =)
            else -> null
        }

        if (minTotalPrice == null && maxTotalPrice != null) {
            minTotalPrice = BigDecimal.ZERO
        }

        if (minTotalPrice != null && maxTotalPrice != null) {
            if (minTotalPrice > maxTotalPrice) {
                maxTotalPrice = BigDecimal(1000000)
            }
        }

        val cityArray = city?.map { it }?.toTypedArray()
        val beachArray = beachName?.map { it }?.toTypedArray()
        val estates = repository.findByParams(
            types = types?.map { it.name }?.toTypedArray(),
            buildEndYears = buildEndYears?.map { it.toString() }?.toTypedArray(),

            isStudioRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("0"),
            isOneRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("1"),
            isTwoRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("2"),
            isFreeRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("3"),
            isFourRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("4"),

            minPrice = minTotalPrice,
            maxPrice = maxTotalPrice,

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

            beachName = beachArray,

            city = cityArray,

            limit = pageable.pageSize,
            offset = pageable.offset
        )

        if (estates.isNotEmpty() && userId != null) {
            val collections = collectionRepository.findAllByParams(
                userId = userId.toString(),
                limit = 100,
                offset = 0
            )
            if (collections.isNotEmpty()) {
                collections.forEach {
                    collection -> collection.collectionDetail.estateIds.forEach { estateId ->
                        estates.forEach { estate ->
                            if (estateId == estate.id) {
                                val count = estate.estateDetail.collectionCount ?: 0
                                estate.estateDetail.collectionCount = count + 1
                            }
                        }
                }
                }
            }
        }

        return estates
    }

    override fun findCount(
        types: Set<EstateType>?,
        buildEndYears: Set<Int>?,
        rooms: Set<String>?,
        price: String?,
        grades: Set<String>?,
        beachTravelTimes: Set<String>?,
        airportTravelTimes: Set<String>?,
        parking: Boolean?,
        managementCompanyEnabled: Boolean?,
        beachName: Set<String>?,
        city: Set<String>?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?
    ): Int {
        var minTotalPrice =  minPrice ?:
        when (price) {
            "1" -> BigDecimal.ZERO
            "2" -> BigDecimal.valueOf(100_000)
            "3" -> BigDecimal.valueOf(200_000)
            "4" -> BigDecimal.valueOf(500_000)
            "5" -> BigDecimal.valueOf(1_000_000)
            else -> null
        }
        var maxTotalPrice = maxPrice ?: when (price) {
            "1" -> BigDecimal.valueOf(100_000)
            "2" -> BigDecimal.valueOf(200_000)
            "3" -> BigDecimal.valueOf(500_000)
            "4" -> BigDecimal.valueOf(1_000_000)
            "5" -> BigDecimal.valueOf(100_000_000_000) // =)
            else -> null
        }

        if (minTotalPrice == null && maxTotalPrice != null) {
            minTotalPrice = BigDecimal.ZERO
        }

        if (minTotalPrice != null && maxTotalPrice != null) {
            if (minTotalPrice > maxTotalPrice) {
                maxTotalPrice = BigDecimal(1000000)
            }
        }

        val cityArray = city?.map { it }?.toTypedArray()
        val beachArray = beachName?.map { it }?.toTypedArray()
        return repository.findByParams(
            types = types?.map { it.name }?.toTypedArray(),
            buildEndYears = buildEndYears?.map { it.toString() }?.toTypedArray(),

            isStudioRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("0"),
            isOneRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("1"),
            isTwoRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("2"),
            isFreeRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("3"),
            isFourRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("4"),

            minPrice = minTotalPrice,
            maxPrice = maxTotalPrice,

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

            beachName = beachArray,

            city = cityArray,

            limit = 500,
            offset = 0
        ).size
    }

    override fun findById(id: UUID): EstateEntity {
        val estateEntity = repository.findById(id).getOrNull()
            ?: throw NotFoundLogicalException(errorCode = ErrorCode.createNotFound("Project object not found"))

        return estateEntity
    }

    override fun saveAll(estates: List<EstateEntity>) {
        repository.saveAll(estates)
    }

    override fun findByIds(ids: Set<UUID>): List<EstateEntity> {
        return repository.findAllById(ids)
    }

    override fun findPages(pageCount: Int, allCount: Int): Int {
        val pages = allCount / pageCount
        if (pages % 2 == 0)
            return pages
        return pages + 1
    }

    override fun aiRequest(rq: String): List<EstateEntity> {
        aiRequestRepository.save(
            AiRequestEntity(
                id = UUID.randomUUID(),
                request = rq
            ).apply { isNew = true }
        )

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

        val currency = rs.currency ?: "USD"
        if (currency === "RUB") {
            minPrice = minPrice.multiply(BigDecimal(85))
            maxPrice = maxPrice.multiply(BigDecimal(85))
        }
        if (currency === "THB") {
            minPrice = minPrice.multiply(BigDecimal(0.03))
            maxPrice = maxPrice.multiply(BigDecimal(0.03))
        }

        val beachWalkTime = try {
            (rs.beachTravelTimesWalk ?: "0").toInt()
        } catch (_: Exception) {
            0
        }

        val airportCarTime = try {
            (rs.airportTravelTimes ?: "0").toInt()
        } catch (_: Exception) {
            0
        }

        val mallTravelTimes = try {
            (rs.mallTravelTimes ?: "0").toInt()
        } catch (_: Exception) {
            0
        }

        var maxBeachWalkTravelTimeOne: Int? = null
        var minBeachWalkTravelTimeTwo: Int? = null
        var maxBeachWalkTravelTimeTwo: Int? = null
        var minBeachWalkTravelTimeFree: Int? = null
        var maxBeachWalkTravelTimeFree: Int? = null

        if (beachWalkTime == 0) {
            maxBeachWalkTravelTimeOne = null
        } else if (beachWalkTime < 6) {
            maxBeachWalkTravelTimeOne = 5
        } else if (beachWalkTime <= 10) {
            minBeachWalkTravelTimeTwo = 0
            maxBeachWalkTravelTimeTwo = 10
        } else if (beachWalkTime <= 30) {
            minBeachWalkTravelTimeFree = 0
            maxBeachWalkTravelTimeFree = 30
        }

        var maxAirportCarTravelTimeOne: Int? = null
        var minAirportCarTravelTimeTwo: Int? = null
        var maxAirportCarTravelTimeTwo: Int? = null
        var maxAirportCarTravelTimeFree: Int? = null
        if (airportCarTime == 0) {
            maxAirportCarTravelTimeOne = null
        } else if (airportCarTime <= 30) {
            maxAirportCarTravelTimeOne = 30
        } else if (airportCarTime <= 60) {
            minAirportCarTravelTimeTwo = 0
            maxAirportCarTravelTimeTwo = 60
        } else if (airportCarTime <= 61) {
            maxAirportCarTravelTimeFree = 61
        }

        if (minPrice == maxPrice) {
            minPrice = BigDecimal.ZERO
        }

        val parking =  if (rs.parking != null && rs.parking.equals("true", ignoreCase = true)) true else null
        var managementCompanyEnabled: Boolean? = null
        if (rs.isUk != null) {
            managementCompanyEnabled = rs.isUk.equals("true", ignoreCase = true)
        }
        var list = repository.findByParams(
            types = type,
            buildEndYears = buildEndYears,
            isStudioRoom = null,
            isOneRoom = null,
            isTwoRoom = null,
            isFreeRoom = null,
            isFourRoom = null,
            minPrice = minPrice,
            maxPrice = maxPrice,
            gradeInvestmentSecurity = null,
            gradeInvestmentPotential = null,
            gradeProjectLocation = null,
            gradeComfortOfLife = null,
            maxBeachWalkTravelTimeOne = maxBeachWalkTravelTimeOne,
            minBeachWalkTravelTimeTwo = minBeachWalkTravelTimeTwo,
            maxBeachWalkTravelTimeTwo = maxBeachWalkTravelTimeTwo,
            minBeachWalkTravelTimeFree = minBeachWalkTravelTimeFree,
            maxBeachWalkTravelTimeFree = maxBeachWalkTravelTimeFree,
            maxBeachCarTravelTimeOne = null,
            minBeachCarTravelTimeTwo = null,
            maxBeachCarTravelTimeTwo = null,
            minBeachCarTravelTimeFree = null,
            maxBeachCarTravelTimeFree = null,
            maxAirportCarTravelTimeOne = maxAirportCarTravelTimeOne,
            minAirportCarTravelTimeTwo = minAirportCarTravelTimeTwo,
            maxAirportCarTravelTimeTwo = maxAirportCarTravelTimeTwo,
            maxAirportCarTravelTimeFree = maxAirportCarTravelTimeFree,
            parking = parking,
            managementCompanyEnabled = managementCompanyEnabled,
            beachName = if (rs.beach != null) arrayOf(rs.beach) else null,
            city = if (rs.city != null) arrayOf(rs.city) else null,
            offset = 0,
            limit = 150,
        )
        if (list.isEmpty())
            list = repository.findRandom()
        if (rs.rating != null) {
            try {
                val rating = BigDecimal(rs.rating)
                list = list.filter {
                    it.estateDetail.grade.main >= rating
                }
            } catch (_: Exception) {}
        }
        if (rs.roi != null && rs.roi.equals("true", ignoreCase = true)) {
            try {
                list = list.sortedByDescending { it.estateDetail.profitability.roi }.take(10)
            } catch (_: Exception) {}
        }
        if (isFourRoom != null && isFourRoom) {
            list = list.filter { it.estateDetail.roomLayouts.four != null }
        }
        if (isOneRoom != null && isOneRoom) {
            list = list.filter { it.estateDetail.roomLayouts.one != null }
        }
        if (isTwoRoom != null && isTwoRoom) {
            list = list.filter { it.estateDetail.roomLayouts.two != null }
        }
        if (isFreeRoom != null && isFreeRoom) {
            list = list.filter { it.estateDetail.roomLayouts.three != null }
        }
        if (isStudioRoom != null && isStudioRoom) {
            list = list.filter { it.estateDetail.roomLayouts.studio != null }
        }
        if (mallTravelTimes > 0) {
            list = list.filter {
                it.estateDetail.infrastructure.mallTime.car < mallTravelTimes ||
                        (it.estateDetail.infrastructure.mallTime.walk != null
                                && it.estateDetail.infrastructure.mallTime.walk!! < mallTravelTimes)
            }
        }
        if (rs.gym != null && rs.gym == "true") {
            list = list.filter {
                it.estateDetail.options.gym
            }
        }
        if (rs.childRoom != null && rs.childRoom == "true") {
            list = list.filter {
                it.estateDetail.options.childRoom
            }
        }
        if (list.isEmpty())
            list = repository.findRandom()
        if (list.size > 50)
            return list.take(20)
        return list
    }

    @PostConstruct
    @Scheduled(cron = "0 0 4 * * *")
    fun range() {
        repository.findRandom().forEach {
            it.updatedAt = LocalDateTime.now()
            repository.save(it)
        }
    }

    override fun createXml(): String {
        val sb = StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<objects>\n")
        repository.findAll().toList().forEach {
            var rooms = 1
            if (it.estateDetail.roomLayouts.villaFive != null || it.estateDetail.roomLayouts.five != null) {
                rooms += 5
            } else if (it.estateDetail.roomLayouts.villaFour != null || it.estateDetail.roomLayouts.four != null) {
                rooms += 4
            } else if (it.estateDetail.roomLayouts.villaThree != null || it.estateDetail.roomLayouts.three != null) {
                rooms += 3
            } else if (it.estateDetail.roomLayouts.villaTwo != null || it.estateDetail.roomLayouts.two != null) {
                rooms += 2
            } else if (it.estateDetail.roomLayouts.one != null) {
                rooms += 1
            }
            val type = when (it.estateDetail.type) {
                EstateType.VILLA -> 6
                else -> 1
            }

            val region = when (it.estateDetail.location.city) {
                "Bangkok" -> 4
                "Phuket" -> 323
                else -> 74
            }
            val status = if (it.estateDetail.buildEndDate != null && it.estateDetail.buildEndDate!!.isAfter(LocalDate.now()))
                2
            else
                1

            val price = it.estateDetail.price.max
            sb.append("   <object>\n")
            sb.append("      <id>${it.id}</id>\n")
            sb.append("      <edited>${it.createdAt}</edited>\n")
            sb.append("      <edited>${it.createdAt}</edited>\n")
            sb.append("      <description>\n" +
                      "         <ru><![CDATA[${it.estateDetail.name}]]></ru>\n" +
                      "         <en><![CDATA[${it.estateDetail.name}]]></en>\n" +
                      "      </description>\n")
            sb.append("      <deal>2</deal>\n" +
                      "      <price>$price</price>\n" +
                      "      <currency>USD</currency>\n" +
                      "      <installment>1</installment>\n" +
                      "      <type>${type}</type>\n" +
                      "      <region>${region}</region>\n" +
                      "      <status>${status}</status>\n" +
                      "      <rooms>${rooms}</rooms>\n" +
                      "      <floors>${it.estateDetail.floors ?: 1}</floors>\n" +
                      "      <tosea>${it.estateDetail.infrastructure.beachTime.walk ?: it.estateDetail.infrastructure.beachTime.car}</tosea>\n" +
                      "      <toairport>${it.estateDetail.infrastructure.airportTime.car}</toairport>\n")
            if (it.estateDetail.buildEndDate != null) {
                val quarter = when (it.estateDetail.buildEndDate?.monthValue) {
                    1, 2, 3 -> 1
                    4, 5, 6 -> 2
                    7, 8, 9 -> 3
                    else -> 4
                }
                sb.append(
                    "      <finish>\n" +
                            "         <year>${it.estateDetail.buildEndDate?.year}</year>\n" +
                            "         <quarter>$quarter</quarter>\n" +
                            "      </finish>\n"
                )
            }

            val images = mutableListOf<String>()
            if (it.estateDetail.interiorImages.isNotNullOrEmpty())
                images.addAll(it.estateDetail.interiorImages!!.toMutableList())
            if (it.estateDetail.exteriorImages.isNotNullOrEmpty())
                images.addAll(it.estateDetail.exteriorImages!!.toMutableList())
            if (it.estateDetail.facilityImages.isNotNullOrEmpty())
                images.addAll(it.estateDetail.facilityImages!!.toMutableList())
            if (images.isNotEmpty()) {
                sb.append("<images>\n")
                images.forEach {
                    image -> sb.append("<image>https://insightestate.pro/estate-images/$image</image>\n")
                }
                sb.append("</images>\n")
            }
            sb.append("   </object>\n")
        }
        sb.append("</objects>\n")
        return sb.toString()
    }
}

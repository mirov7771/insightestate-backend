package ru.nemodev.insightestate.api.client.v1.processor

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import ru.nemodev.insightestate.api.auth.v1.dto.CustomPageDtoRs
import ru.nemodev.insightestate.api.client.v1.converter.EstateDetailDtoRsConverter
import ru.nemodev.insightestate.api.client.v1.converter.EstateDtoRsConverter
import ru.nemodev.insightestate.api.client.v1.dto.estate.*
import ru.nemodev.insightestate.api.client.v1.dto.user.MainInfoDto
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.insightestate.repository.UnitRepository
import ru.nemodev.insightestate.service.estate.EstateImageLoader
import ru.nemodev.insightestate.service.estate.EstateLoader
import ru.nemodev.insightestate.service.estate.EstateService
import ru.nemodev.platform.core.extensions.isNotNullOrEmpty
import java.math.BigDecimal
import java.util.*

interface EstateProcessor {

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
        minPrice: BigDecimal? = null,
        maxPrice: BigDecimal? = null,
        pageable: Pageable,
        userId: UUID? = null,
        name: String? = null,
    ): CustomPageDtoRs

    fun findById(
        id: UUID
    ): EstateDetailDtoRs

    fun loadFromFile(filePart: MultipartFile)
    fun loadFromGoogle()
    fun loadImagesFromDir()
    fun loadImagesFromGoogleDrive()

    fun aiRequest(rq: AiRequest): CustomPageDtoRs
    fun geo(): GeoRs
    fun findUnits(
        id: UUID,
        orderBy: String?,
        rooms: Set<String>?,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        minSize: Double? = null,
        maxSize: Double? = null,
        minPriceSq: Double? = null,
        maxPriceSq: Double? = null
    ): UnitsRs
    fun getMainInfo(userId: UUID): MainInfoDto

    fun prepareXml(): String
}

@Component
class EstateProcessorImpl(
    private val estateService: EstateService,
    private val estateDtoRsConverter: EstateDtoRsConverter,
    private val estateDetailDtoRsConverter: EstateDetailDtoRsConverter,

    private val estateLoader: EstateLoader,
    private val estateImageLoader: EstateImageLoader,
    private val unitRepository: UnitRepository
) : EstateProcessor {

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
        userId: UUID?,
        name: String?
    ): CustomPageDtoRs {
        val estates = estateService.findAll(
            types = types,
            buildEndYears = buildEndYears,
            rooms = rooms,
            price = price,
            grades = grades,
            beachTravelTimes = beachTravelTimes,
            airportTravelTimes = airportTravelTimes,
            parking = parking,
            managementCompanyEnabled = managementCompanyEnabled,
            beachName = beachName,
            city = city,
            minPrice = minPrice,
            maxPrice = maxPrice,
            pageable = pageable,
            userId = userId,
            name = name
        )

        val count = estateService.findCount(
            types = types,
            buildEndYears = buildEndYears,
            rooms = rooms,
            price = price,
            grades = grades,
            beachTravelTimes = beachTravelTimes,
            airportTravelTimes = airportTravelTimes,
            parking = parking,
            managementCompanyEnabled = managementCompanyEnabled,
            beachName = beachName,
            city = city,
            minPrice = minPrice,
            maxPrice = maxPrice,
            name = name
        )

        return CustomPageDtoRs(
            items = estates.map { estateDtoRsConverter.convert(it) },
            pageSize = estates.size,
            pageNumber = pageable.pageNumber,
            hasMore = estates.size >= pageable.pageSize,
            totalPages = estateService.findPages(pageable.pageSize, count),
            totalCount = count
        )
    }

    override fun findById(id: UUID): EstateDetailDtoRs {
        return estateDetailDtoRsConverter.convert(
            estateService.findById(id)
        )
    }

    override fun loadFromFile(filePart: MultipartFile) {
        estateLoader.loadFromFile(filePart)
    }

    override fun loadFromGoogle() {
        estateLoader.loadFromGoogleSpreadsheets()
    }

    override fun loadImagesFromDir() {
        estateImageLoader.loadFromDir()
    }

    override fun loadImagesFromGoogleDrive() {
        estateImageLoader.loadFromGoogleDrive()
    }

    override fun aiRequest(rq: AiRequest): CustomPageDtoRs {
        val estates = estateService.aiRequest(
            String(Base64.getDecoder().decode(rq.request))
        )
        return CustomPageDtoRs(
            items = estates.map { estateDtoRsConverter.convert(it) },
            pageSize = estates.size,
            pageNumber = 1,
            hasMore = false,
            totalPages = 0
        )
    }

    override fun geo(): GeoRs {
        val list = estateService.findAll().map {
            GeoDto(
                id = it.id,
                lat = getLat(it.estateDetail.location.mapUrl),
                lng = getLng(it.estateDetail.location.mapUrl)
            )
        }
        return GeoRs(
            geo = list.filter { it.lat != null && it.lng != null },
        )
    }

    override fun findUnits(
        id: UUID,
        orderBy: String?,
        rooms: Set<String>?,
        minPrice: Double?,
        maxPrice: Double?,
        minSize: Double?,
        maxSize: Double?,
        minPriceSq: Double?,
        maxPriceSq: Double?
    ): UnitsRs {
        val estate = estateService.findById(id)
        val projectId = "${estate.estateDetail.projectId}%"
        var units = unitRepository.findByProjectId(projectId)

        val counts = units.size
        if (units.isNotEmpty()) {
            if (orderBy != null) {
                units = when (orderBy.lowercase()) {
                    "price" -> units.sortedByDescending { stringToDouble(it.price) }
                    "area" -> units.sortedByDescending { stringToDouble(it.square) }
                    "income" -> units.sortedByDescending { stringToDouble(it.priceSq) }
                    "payback" -> units.sortedByDescending { it.rooms }
                    else -> units
                }
            }
            if (rooms.isNotNullOrEmpty()) {
                units = units.filter { it.rooms != null && rooms!!.contains(it.rooms) }
            }

            if (minPrice != null) {
                units = units.filter { stringToDouble(it.price) >= minPrice }
            }
            if (maxPrice != null) {
                units = units.filter { stringToDouble(it.price) <= maxPrice }
            }
            if (minSize != null) {
                units = units.filter { stringToDouble(it.square) >= minSize }
            }
            if (maxSize != null) {
                units = units.filter { stringToDouble(it.square) <= maxSize }
            }
            if (minPriceSq != null) {
                units = units.filter { stringToDouble(it.priceSq) >= minPriceSq }
            }
            if (maxPriceSq != null) {
                units = units.filter { stringToDouble(it.priceSq) >= maxPriceSq }
            }
        }

        return UnitsRs(
            id = estate.id,
            name = estate.estateDetail.name,
            images = estate.estateDetail.exteriorImages ?: estate.estateDetail.facilityImages ?: estate.estateDetail.interiorImages,
            items = units,
            count = counts,
        )
    }

    private fun stringToDouble(value: String?): Double {
        if (value == null) {
            return 0.0
        }
        return try {
            value.replace(" ", "")
                .replace(" ", "")
                .replace(",", ".")
                .toDouble()
        } catch (_: Exception) {
            0.0
        }
    }

    override fun getMainInfo(userId: UUID): MainInfoDto {
        val list = estateService.findAll()
        val units = unitRepository.findAll().toList()
        return MainInfoDto(
            collections = unitRepository.findAllCollections(userId.toString()),
            units = units.size,
            objects = list.size,
            bestObjects = list.filter { it.estateDetail.grade.main >= BigDecimal(9) }.size,
        )
    }

    override fun prepareXml(): String {
        return estateService.createXml()
    }

    private fun getLat(url: String): String? {
        val split = url.split("@")
        if (split.size < 2)
            return null
        val s = split[1].split(",")
        if (s.size < 2)
            return null
        return s[0]
    }

    private fun getLng(url: String): String? {
        val split = url.split("@")
        if (split.size < 2)
            return null
        val s = split[1].split(",")
        if (s.size < 2)
            return null
        return s[1]
    }
}

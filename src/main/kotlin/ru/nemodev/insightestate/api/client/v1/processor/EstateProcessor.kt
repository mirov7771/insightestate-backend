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
    fun findUnits(id: UUID): UnitsRs
    fun getMainInfo(userId: UUID): MainInfoDto
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
        userId: UUID?
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
            userId = userId
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
            maxPrice = maxPrice
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

    override fun findUnits(id: UUID): UnitsRs {
        val estate = estateService.findById(id)
        val projectId = "${estate.estateDetail.projectId}%"

        return UnitsRs(
            id = estate.id,
            name = estate.estateDetail.name,
            images = estate.estateDetail.exteriorImages ?: estate.estateDetail.facilityImages ?: estate.estateDetail.interiorImages,
            items = unitRepository.findByProjectId(projectId)
        )
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

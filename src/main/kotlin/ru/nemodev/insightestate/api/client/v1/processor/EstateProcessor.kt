package ru.nemodev.insightestate.api.client.v1.processor

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import ru.nemodev.insightestate.api.auth.v1.dto.CustomPageDtoRs
import ru.nemodev.insightestate.api.client.v1.converter.EstateDetailDtoRsConverter
import ru.nemodev.insightestate.api.client.v1.converter.EstateDtoRsConverter
import ru.nemodev.insightestate.api.client.v1.dto.estate.AiRequest
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDetailDtoRs
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.insightestate.service.estate.EstateImageLoader
import ru.nemodev.insightestate.service.estate.EstateLoader
import ru.nemodev.insightestate.service.estate.EstateService
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
        beachName: String?,
        city: String?,
        pageable: Pageable
    ): CustomPageDtoRs

    fun findById(
        id: UUID
    ): EstateDetailDtoRs

    fun loadFromFile(filePart: MultipartFile)
    fun loadFromGoogle()
    fun loadImagesFromDir()
    fun loadImagesFromGoogleDrive()

    fun aiRequest(rq: AiRequest): CustomPageDtoRs
}

@Component
class EstateProcessorImpl(
    private val estateService: EstateService,
    private val estateDtoRsConverter: EstateDtoRsConverter,
    private val estateDetailDtoRsConverter: EstateDetailDtoRsConverter,

    private val estateLoader: EstateLoader,
    private val estateImageLoader: EstateImageLoader
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
        beachName: String?,
        city: String?,
        pageable: Pageable
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
            pageable = pageable
        )

        return CustomPageDtoRs(
            items = estates.map { estateDtoRsConverter.convert(it) },
            pageSize = estates.size,
            pageNumber = pageable.pageNumber,
            hasMore = estates.size >= pageable.pageSize,
            totalPages = estateService.findPages(pageable.pageSize)
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
}

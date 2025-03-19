package ru.nemodev.insightestate.api.client.v1.processor

import org.springframework.core.io.InputStreamResource
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import ru.nemodev.insightestate.api.auth.v1.dto.CustomPageDtoRs
import ru.nemodev.insightestate.api.client.v1.converter.EstateDetailDtoRsConverter
import ru.nemodev.insightestate.api.client.v1.converter.EstateDtoRsConverter
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDetailDtoRs
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.insightestate.service.estate.EstateImageLoader
import ru.nemodev.insightestate.service.estate.EstateLoader
import ru.nemodev.insightestate.service.estate.EstateService
import ru.nemodev.insightestate.service.estate.EstateWebFlowCsvExporter
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
        pageable: Pageable
    ): CustomPageDtoRs

    fun findById(
        id: UUID
    ): EstateDetailDtoRs

    fun loadFromFile(filePart: MultipartFile)
    fun loadImagesFromDir()

    fun downloadWebFlowCsvFile(): ResponseEntity<InputStreamResource>
}

@Component
class EstateProcessorImpl(
    private val estateService: EstateService,
    private val estateDtoRsConverter: EstateDtoRsConverter,
    private val estateDetailDtoRsConverter: EstateDetailDtoRsConverter,

    private val estateLoader: EstateLoader,
    private val estateImageLoader: EstateImageLoader,
    private val estateWebFlowCsvExporter: EstateWebFlowCsvExporter
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
        pageable: Pageable
    ): CustomPageDtoRs {
        val estates = estateService.findAll(
            types = types,
            buildEndYears = buildEndYears,
            rooms = rooms,
            price = price,
            grades = grades,
            beachTravelTimes = beachTravelTimes,
            beachName = beachName,
            airportTravelTimes = airportTravelTimes,
            parking = parking,
            managementCompanyEnabled = managementCompanyEnabled,
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

    override fun loadImagesFromDir() {
        estateImageLoader.loadFromDir()
    }

    override fun downloadWebFlowCsvFile(): ResponseEntity<InputStreamResource> {
        return estateWebFlowCsvExporter.export().toResponseEntity()
    }

}

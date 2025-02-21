package ru.nemodev.insightestate.api.client.v1.processor

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import ru.nemodev.insightestate.api.client.v1.converter.EstateDetailDtoRsConverter
import ru.nemodev.insightestate.api.client.v1.converter.EstateDtoRsConverter
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDetailDtoRs
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDtoRs
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.insightestate.service.estate.EstateImageLoader
import ru.nemodev.insightestate.service.estate.EstateLoader
import ru.nemodev.insightestate.service.estate.EstateService
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
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
        pageable: Pageable
    ): PageDtoRs<EstateDtoRs>

    fun findById(
        id: UUID
    ): EstateDetailDtoRs

    fun loadFromFile(filePart: MultipartFile)
    fun loadImagesFromDir()
}

@Component
class EstateProcessorImpl(
    private val estateService: EstateService,
    private val estateDtoRsConverter: EstateDtoRsConverter,
    private val estateDetailDtoRsConverter: EstateDetailDtoRsConverter,

    private val estateLoader: EstateLoader,
    private val estateImageLoader: EstateImageLoader,
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
        pageable: Pageable
    ): PageDtoRs<EstateDtoRs> {
        val estateList = estateService.findAll(
            types = types,
            buildEndYears = buildEndYears,
            rooms = rooms,
            price = price,
            grades = grades,
            beachTravelTimes = beachTravelTimes,
            airportTravelTimes = airportTravelTimes,
            parking = parking,
            pageable = pageable
        )

        return PageDtoRs(
            items = estateList.map { estateDtoRsConverter.convert(it) },
            pageSize = estateList.size,
            pageNumber = pageable.pageNumber,
            hasMore = estateList.size >= pageable.pageSize
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

}
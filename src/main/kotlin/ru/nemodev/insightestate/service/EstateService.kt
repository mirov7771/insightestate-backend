package ru.nemodev.insightestate.service

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.api.client.v1.dto.EstateInfoDto
import ru.nemodev.insightestate.api.client.v1.dto.EstateListDto
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.insightestate.repository.EstateRepository
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.NotFoundLogicalException
import ru.nemodev.platform.core.extensions.isNotNullOrEmpty
import java.util.*

interface EstateService {
    fun findAll(
        potential: List<Int>?,
        price: Long?,
        beds: List<String>?,
        year: List<Int>?,
        type: EstateType?,
        pageable: PageRequest
    ): PageDtoRs<EstateListDto>

    fun findById(
        id: UUID
    ): EstateInfoDto
}

@Service
class EstateServiceImpl (
    private val repository: EstateRepository,
) : EstateService {
    override fun findAll(
        potential: List<Int>?,
        price: Long?,
        beds: List<String>?,
        year: List<Int>?,
        type: EstateType?,
        pageable: PageRequest
    ): PageDtoRs<EstateListDto> {
        var priceStart: Long? = null
        var priceEnd: Long? = null
        if (price != null) {
            priceEnd = price
            priceStart = if (priceEnd <= 100000) {
                0
            } else if (priceEnd <= 200000) {
                100000
            } else if (priceEnd <= 500000) {
                200000
            } else if (priceEnd <= 1000000) {
                500000
            } else {
                1000000
            }
        }

        var isStudio: String? = null
        var isOneRoom: String? = null
        var isTwoRoom: String? = null
        var isThreeRoom: String? = null
        var isFourRoom: String? = null
        var isFiveRoom: String? = null

        if (beds.isNotNullOrEmpty()) {
            isStudio = beds!!.contains("Студия").toString()
            isOneRoom = beds.contains("1").toString()
            isTwoRoom = beds.contains("2").toString()
            isThreeRoom = beds.contains("3").toString()
            isFourRoom = beds.contains("4").toString()
            isFiveRoom = beds.contains("4+").toString()
        }
        var estateType: String? = null
        if (type != null)
            estateType = type.toString()
        var attachmentSecurity: Int? = null
        var investmentPotential: Int? = null
        var locationOfTheObject: Int? = null
        var comfortOfLife: Int? = null
        if (potential.isNotNullOrEmpty()) {
            attachmentSecurity = if (potential!!.contains(0)) 9 else null
            investmentPotential = if (potential.contains(1)) 9 else null
            locationOfTheObject = if (potential.contains(2)) 9 else null
            comfortOfLife = if (potential.contains(3)) 9 else null
        }
        val list = repository.findByParams(
            isStudio = isStudio,
            isOneRoom = isOneRoom,
            isTwoRoom = isTwoRoom,
            isThreeRoom = isThreeRoom,
            isFourRoom = isFourRoom,
            isFiveRoom = isFiveRoom,
            priceStart = priceStart,
            priceEnd = priceEnd,
            type = estateType,
            attachmentSecurity = attachmentSecurity,
            investmentPotential = investmentPotential,
            locationOfTheObject = locationOfTheObject,
            comfortOfLife = comfortOfLife,
            limit = pageable.pageSize,
            offset = pageable.offset
        )
        return PageDtoRs(
            items = list.map { EstateListDto(
                id = it.id,
                rate = it.estateDetail.rate,
                name = it.estateDetail.name,
                price = it.estateDetail.priceStart,
                profitAmount = it.estateDetail.profitAmount,
                profitTerm = it.estateDetail.profitTerm,
                images = it.estateDetail.images ?: emptyList(),
                level = it.estateDetail.level,
                beach = it.estateDetail.beach,
                deliveryDate = it.estateDetail.deliveryDate
            ) },
            pageSize = list.size,
            pageNumber = pageable.pageNumber,
            hasMore = list.size >= pageable.pageSize
        )
    }

    override fun findById(
        id: UUID
    ): EstateInfoDto {
        val dao = repository.findById(id).orElseThrow {
            NotFoundLogicalException(errorCode = ErrorCode.createNotFound("Объект не найден"))
        }
        return EstateInfoDto(
            id = dao.id,
            rate = dao.estateDetail.rate,
            name = dao.estateDetail.name,
            profitAmount = dao.estateDetail.profitAmount,
            profitTerm = dao.estateDetail.profitTerm,
            images = dao.estateDetail.images ?: emptyList(),
            type = dao.estateDetail.type,
            square = dao.estateDetail.square,
            attachmentSecurity = dao.estateDetail.attachmentSecurity,
            investmentPotential = dao.estateDetail.investmentPotential,
            locationOfTheObject = dao.estateDetail.locationOfTheObject,
            comfortOfLife = dao.estateDetail.comfortOfLife,
            deliveryDate = dao.estateDetail.deliveryDate,
            floors = dao.estateDetail.floors,
            apartments = dao.estateDetail.apartments,
            beach = dao.estateDetail.beach,
            airport = dao.estateDetail.airport,
            parking = dao.estateDetail.parking,
            developer = dao.estateDetail.developer,
            level = dao.estateDetail.level,
            mall = dao.estateDetail.mall,
            childRoom = dao.estateDetail.childRoom,
            coWorking = dao.estateDetail.coWorking,
            gym = dao.estateDetail.gym,
            rentalIncome = dao.estateDetail.rentalIncome,
            roi = dao.estateDetail.roi,
            irr = dao.estateDetail.irr,
            projectImage = dao.estateDetail.projectImage,
            district = dao.estateDetail.district,
            geoPosition = dao.estateDetail.geoPosition,
        )
    }
}

package ru.bd.platform.insightestate.service

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import ru.bd.platform.insightestate.api.v1.dto.EstateInfoDto
import ru.bd.platform.insightestate.api.v1.dto.EstateListDto
import ru.bd.platform.insightestate.entity.EstateType
import ru.bd.platform.insightestate.repository.EstateRepository
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.NotFoundLogicalException
import java.util.*

interface EstateService {
    fun findAll(
        potential: List<Int>?,
        price: Long?,
        beds: List<Int>?,
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
        beds: List<Int>?,
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
        val list = repository.findByParams(
            priceStart = priceStart,
            priceEnd = priceEnd,
            type = type.toString(),
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
                images = it.estateDetail.images
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
            priceStart = dao.estateDetail.priceStart,
            priceEnd = dao.estateDetail.priceEnd,
            profitAmount = dao.estateDetail.profitAmount,
            profitTerm = dao.estateDetail.profitTerm,
            images = dao.estateDetail.images,
            type = dao.estateDetail.type,
            squareStart = dao.estateDetail.squareStart,
            squareEnd = dao.estateDetail.squareEnd,
            beds = dao.estateDetail.beds,
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
        )
    }
}

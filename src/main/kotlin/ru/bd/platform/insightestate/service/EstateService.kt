package ru.bd.platform.insightestate.service

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import ru.bd.platform.insightestate.api.v1.dto.EstateInfoDto
import ru.bd.platform.insightestate.api.v1.dto.EstateListDto
import ru.bd.platform.insightestate.entity.EstateType
import ru.bd.platform.insightestate.repository.EstateRepository
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
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
        TODO("Not yet implemented")
    }

    override fun findById(
        id: UUID
    ): EstateInfoDto {
        TODO("Not yet implemented")
    }
}

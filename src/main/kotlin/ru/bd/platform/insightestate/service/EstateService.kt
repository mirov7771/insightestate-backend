package ru.bd.platform.insightestate.service

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import ru.bd.platform.insightestate.api.v1.dto.EstateListDto
import ru.bd.platform.insightestate.entity.EstateType
import ru.bd.platform.insightestate.repository.EstateRepository
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs

interface EstateService {
    fun findAll(
        bads: Int?,
        year: Int?,
        type: EstateType?,
        pageable: PageRequest
    ): PageDtoRs<EstateListDto>
}

@Service
class EstateServiceImpl (
    private val repository: EstateRepository,
) : EstateService {
    override fun findAll(
        bads: Int?,
        year: Int?,
        type: EstateType?,
        pageable: PageRequest
    ): PageDtoRs<EstateListDto> {
        TODO("Not yet implemented")
    }
}

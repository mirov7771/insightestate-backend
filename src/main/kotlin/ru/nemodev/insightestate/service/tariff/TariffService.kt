package ru.nemodev.insightestate.service.tariff

import org.springframework.stereotype.Service
import ru.nemodev.insightestate.entity.TariffEntity
import ru.nemodev.insightestate.repository.TariffRepository

interface TariffService {
    fun findAll(): List<TariffEntity>
}

@Service
class TariffServiceImpl (
    private val tariffRepository: TariffRepository
) : TariffService {
    override fun findAll(): List<TariffEntity> {
        return tariffRepository.findAll()
    }
}

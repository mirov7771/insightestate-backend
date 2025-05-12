package ru.nemodev.insightestate.service.tariff

import org.springframework.stereotype.Service
import ru.nemodev.insightestate.entity.TariffEntity
import ru.nemodev.insightestate.repository.TariffRepository
import java.util.*

interface TariffService {
    fun findAll(): List<TariffEntity>
    fun findById(id: UUID): TariffEntity
}

@Service
class TariffServiceImpl (
    private val tariffRepository: TariffRepository
) : TariffService {
    override fun findAll(): List<TariffEntity> {
        return tariffRepository.findAll().sortedBy { it.price }
    }

    override fun findById(id: UUID): TariffEntity {
        return tariffRepository.findById(id).get()
    }
}

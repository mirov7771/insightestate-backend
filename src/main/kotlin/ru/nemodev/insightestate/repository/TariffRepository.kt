package ru.nemodev.insightestate.repository

import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.TariffEntity
import java.util.*

@Repository
interface TariffRepository: ListCrudRepository<TariffEntity, UUID>

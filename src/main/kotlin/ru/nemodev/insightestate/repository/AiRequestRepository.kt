package ru.nemodev.insightestate.repository

import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.AiRequestEntity
import java.util.UUID

@Repository
interface AiRequestRepository: ListCrudRepository<AiRequestEntity, UUID>

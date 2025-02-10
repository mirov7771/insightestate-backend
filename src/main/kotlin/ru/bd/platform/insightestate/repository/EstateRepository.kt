package ru.bd.platform.insightestate.repository

import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.bd.platform.insightestate.entity.EstateEntity
import java.util.UUID

@Repository
interface EstateRepository: ListCrudRepository<EstateEntity, UUID> {
}

package ru.nemodev.insightestate.repository

import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.StripeUserEntity
import java.util.UUID

@Repository
interface StripeUserRepository: ListCrudRepository<StripeUserEntity, UUID> {
    fun findByUserId(userId: UUID): StripeUserEntity?
}

package ru.nemodev.insightestate.repository

import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.SubscriptionEntity
import java.util.*

@Repository
interface SubscriptionRepository: ListCrudRepository<SubscriptionEntity, UUID> {
    fun findByUserId(userId: UUID): SubscriptionEntity?
}

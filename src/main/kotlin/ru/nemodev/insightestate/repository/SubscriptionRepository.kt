package ru.nemodev.insightestate.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.SubscriptionEntity
import java.time.LocalDateTime
import java.util.*

@Repository
interface SubscriptionRepository: ListCrudRepository<SubscriptionEntity, UUID> {
    fun findByUserId(userId: UUID): SubscriptionEntity?

    @Query("""
        select * 
          from subscription s  
         where main_pay_date > :dateStart
           and main_pay_date < :dateEnd
    """)
    fun findPaymentsToPay(
        dateStart: LocalDateTime,
        dateEnd: LocalDateTime
    ): List<SubscriptionEntity>
}

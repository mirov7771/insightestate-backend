package ru.nemodev.insightestate.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.UserEntity
import ru.nemodev.insightestate.entity.UserStatus
import java.util.*

@Repository
interface UserRepository : ListCrudRepository<UserEntity, UUID> {

    @Query("""
        select * from users
        where user_detail ->> 'login' = :login
            and (:status is null or user_detail ->> 'status' = :status)
    """)
    fun findByLogin(
        login: String,
        status: UserStatus?,
    ): UserEntity?
}
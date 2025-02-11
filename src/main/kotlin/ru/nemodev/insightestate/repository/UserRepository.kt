package ru.nemodev.insightestate.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.UserEntity
import java.util.*

@Repository
interface UserRepository : ListCrudRepository<UserEntity, UUID> {

    @Query("""
        select * from users
        where user_detail ->> 'login' = :login
    """)
    fun findByLogin(
        login: String
    ): UserEntity?
}
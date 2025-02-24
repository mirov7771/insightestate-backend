package ru.nemodev.insightestate.api.client.v1.processor

import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.dto.user.UserDtoRs
import ru.nemodev.insightestate.api.client.v1.dto.user.UserUpdateDtoRq
import ru.nemodev.insightestate.service.UserService

interface UserProcessor {
    fun getUser(authBasicToken: String): UserDtoRs
    fun update(authBasicToken: String, request: UserUpdateDtoRq)
}

@Component
class UserProcessorImpl(
    private val userService: UserService,
) : UserProcessor {

    override fun getUser(authBasicToken: String): UserDtoRs {
        val userEntity = userService.getUser(authBasicToken)
        return UserDtoRs(
            login = userEntity.userDetail.login,
            fio = userEntity.userDetail.fio!!,
            mobileNumber = userEntity.userDetail.mobileNumber!!,
            location = userEntity.userDetail.location!!
        )
    }

    override fun update(authBasicToken: String, request: UserUpdateDtoRq) {
        userService.update(authBasicToken, request)
    }
}
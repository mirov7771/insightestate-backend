package ru.nemodev.insightestate.api.client.v1.processor

import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.dto.user.HelpWithClientRq
import ru.nemodev.insightestate.api.client.v1.dto.user.UserDtoRs
import ru.nemodev.insightestate.api.client.v1.dto.user.UserUpdateDtoRq
import ru.nemodev.insightestate.service.UserService

interface UserProcessor {
    fun getUser(authBasicToken: String): UserDtoRs
    fun update(authBasicToken: String, request: UserUpdateDtoRq)
    fun helpWithClient(authBasicToken: String, request: HelpWithClientRq)
    fun deleteUser(authBasicToken: String)
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
            location = userEntity.userDetail.location!!,
            whatsUp = userEntity.userDetail.whatsUp,
            tgName = userEntity.userDetail.tgName,
            profileImage = userEntity.userDetail.profileImage,
            id = userEntity.id,
            group = userEntity.userDetail.group
        )
    }

    override fun update(authBasicToken: String, request: UserUpdateDtoRq) {
        userService.update(authBasicToken, request)
    }

    override fun helpWithClient(authBasicToken: String, request: HelpWithClientRq) {
        userService.helpWithClient(authBasicToken, request)
    }

    override fun deleteUser(authBasicToken: String) {
        userService.deleteUser(authBasicToken)
    }
}

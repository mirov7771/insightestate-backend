package ru.nemodev.insightestate.service

import org.springframework.context.annotation.Lazy
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.api.client.v1.dto.user.HelpWithClientRq
import ru.nemodev.insightestate.api.client.v1.dto.user.UserUpdateDtoRq
import ru.nemodev.insightestate.entity.UserDetail
import ru.nemodev.insightestate.entity.UserEntity
import ru.nemodev.insightestate.entity.UserStatus
import ru.nemodev.insightestate.extension.toAuthenticationToken
import ru.nemodev.insightestate.repository.EstateRepository
import ru.nemodev.insightestate.repository.UserRepository
import ru.nemodev.insightestate.service.security.UserAuthenticationManager
import java.util.*
import kotlin.jvm.optionals.getOrNull

interface UserService : UserDetailsService {
    fun findByLogin(login: String, status: UserStatus? = null): UserEntity?
    fun create(login: String, signUpConfirmCode: String): UserEntity
    fun update(userEntity: UserEntity): UserEntity
    fun update(authBasicToken: String, request: UserUpdateDtoRq)
    fun getUser(authBasicToken: String): UserEntity

    // TODO удалить? Временно на мвп
    fun createWithoutConfirm(login: String): UserEntity
    fun helpWithClient(authBasicToken: String, request: HelpWithClientRq)
}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    @Lazy // TODO сделать spring security authentication тогда не придется в каждом методе принимать токен и искать клиента
    private val authManager: UserAuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
    private val repository: EstateRepository,
    private val emailService: EmailService
) : UserService {

    override fun findByLogin(login: String, status: UserStatus?): UserEntity? {
        return userRepository.findByLogin(login = login, status = status)
    }

    override fun create(login: String, signUpConfirmCode: String): UserEntity {
        return userRepository.save(
            UserEntity(
                userDetail = UserDetail(
                    login = login,
                    signUpConfirmCode = signUpConfirmCode,
                    status = UserStatus.SIGN_UP_CONFIRM_CODE,
                )
            )
        )
    }

    override fun update(userEntity: UserEntity): UserEntity {
        return userRepository.save(userEntity)
    }

    override fun update(authBasicToken: String, request: UserUpdateDtoRq) {
        val userEntity = getUser(authBasicToken)
        userEntity.userDetail.fio = request.fio
        userEntity.userDetail.mobileNumber = request.mobileNumber
        userEntity.userDetail.location = request.location
        request.password?.let {
            userEntity.userDetail.passwordHash = passwordEncoder.encode(request.password)
        }

        update(userEntity)
    }

    override fun getUser(authBasicToken: String): UserEntity {
        val authToken = authBasicToken.toAuthenticationToken()
        val authentication = authManager.authenticateWithoutCheckPassword(authToken)

        val userEntity = authentication.principal as UserEntity

        return userEntity
    }

    override fun loadUserByUsername(username: String): UserDetails {
        return findByLogin(login = username, status = UserStatus.ACTIVE)
            ?: throw UsernameNotFoundException("Active user $username not found")
    }

    override fun createWithoutConfirm(login: String): UserEntity {
        return userRepository.save(
            UserEntity(
                userDetail = UserDetail(
                    login = login,
                    signUpConfirmCode = "0000",
                    status = UserStatus.SIGN_UP_CONFIRMED,
                )
            )
        )
    }

    override fun helpWithClient(authBasicToken: String, request: HelpWithClientRq) {
        val userEntity = getUser(authBasicToken)
        val estate = repository.findById(UUID.fromString(request.objectId)).getOrNull()
        emailService.sendToAdmin(
            subject = "Заявка на помощь с клиентом",
            message = "Оставлена заявка на помощь с клиентом: \n" +
                    "- Агент: ${userEntity.userDetail.fio} ${userEntity.userDetail.login}\n" +
                    "- Объект: ${estate?.estateDetail?.projectId} ${estate?.estateDetail?.name}\n" +
                    "- Имя клиента: ${request.name} ${request.lastName}\n" +
                    "- Телефон клиента: ${request.phone}\n" +
                    "- Страна и город клиента: ${request.location}\n"
        )
    }
}

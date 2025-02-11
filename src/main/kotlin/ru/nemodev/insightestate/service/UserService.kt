package ru.nemodev.insightestate.service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.entity.UserDetailEntity
import ru.nemodev.insightestate.entity.UserEntity
import ru.nemodev.insightestate.entity.UserStatus
import ru.nemodev.insightestate.repository.UserRepository

interface UserService : UserDetailsService {
    fun findByLogin(login: String): UserEntity?
    fun create(login: String, signUpConfirmCode: String): UserEntity
    fun update(userEntity: UserEntity): UserEntity
}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
) : UserService {

    override fun findByLogin(login: String): UserEntity? {
        return userRepository.findByLogin(login = login)
    }

    override fun create(login: String, signUpConfirmCode: String): UserEntity {
        return userRepository.save(
            UserEntity(
                userDetail = UserDetailEntity(
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

    override fun loadUserByUsername(username: String): UserDetails {
        return findByLogin(login = username)
            ?: throw UsernameNotFoundException("Пользователь $username не найден")
    }

}

package ru.nemodev.insightestate.service

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.api.auth.v1.dto.SignUpConfirmCodeDtoRq
import ru.nemodev.insightestate.api.auth.v1.dto.SignUpDtoRq
import ru.nemodev.insightestate.api.auth.v1.dto.SignUpEndDtoRq
import ru.nemodev.insightestate.entity.UserStatus
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.LogicException
import ru.nemodev.platform.core.exception.logic.NotFoundLogicalException
import ru.nemodev.platform.core.exception.logic.ValidationLogicException

interface AuthService {
    fun signUp(request: SignUpDtoRq)
    fun signUpCheckConfirmCode(request: SignUpConfirmCodeDtoRq)
    fun signUpSendNewConfirmCode(request: SignUpDtoRq)
    fun signUpEnd(request: SignUpEndDtoRq)
}

@Service
class AuthServiceImpl(
    private val userService: UserService,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    private val confirmCodeGenerator: ConfirmCodeGenerator
) : AuthService {

    override fun signUp(request: SignUpDtoRq) {
        val userEntity = userService.findByLogin(request.login)
        val signUpConfirmCode = confirmCodeGenerator.generateDigits()

        if (userEntity != null) {
            throw LogicException(
                errorCode = ErrorCode.create(HttpStatus.CONFLICT.value().toString(), "Client with email already sign up, please try sign in or send confirm registration code"),
                httpStatus = HttpStatus.CONFLICT
            )
        } else {
            userService.create(
                login = request.login,
                signUpConfirmCode = signUpConfirmCode,
            )
            emailService.signUpSendConfirmCode(
                email = request.login,
                confirmCode = signUpConfirmCode
            )
        }
    }

    override fun signUpCheckConfirmCode(request: SignUpConfirmCodeDtoRq) {
        val userEntity = userService.findByLogin(request.login)
            ?: throw NotFoundLogicalException(
                errorCode = ErrorCode.createNotFound("User with email ${request.login} not found")
            )

        if (!userEntity.userDetail.status.isSignUpConfirmCode()) {
            throw ValidationLogicException(
                errorCode = ErrorCode.createValidation("User with email ${request.login} already confirmed sign up, please try sign in")
            )
        }

        if (userEntity.userDetail.signUpConfirmCode != request.confirmCode) {
            throw ValidationLogicException(
                errorCode = ErrorCode.createValidation("Confirmation code is not valid, please check your code and try again")
            )
        }

        userEntity.userDetail.status = UserStatus.SIGN_UP_CONFIRMED
        userService.update(userEntity)
    }

    override fun signUpSendNewConfirmCode(request: SignUpDtoRq) {
        val userEntity = userService.findByLogin(request.login)
            ?: throw NotFoundLogicalException(
                errorCode = ErrorCode.createNotFound("User with email ${request.login} not found")
            )

        if (!userEntity.userDetail.status.isSignUpConfirmCode()) {
            throw ValidationLogicException(
                errorCode = ErrorCode.createValidation("User with email ${request.login} already confirmed sign up, please try sign in")
            )
        }

        val signUpConfirmCode = confirmCodeGenerator.generateDigits()
        userEntity.userDetail.signUpConfirmCode = signUpConfirmCode
        userService.update(userEntity)
        emailService.signUpSendConfirmCode(userEntity.userDetail.login, signUpConfirmCode)
    }

    override fun signUpEnd(request: SignUpEndDtoRq) {
        val userEntity = userService.findByLogin(request.login)
            ?: throw NotFoundLogicalException(
                errorCode = ErrorCode.createNotFound("User with email ${request.login} not found")
            )

        if (userEntity.userDetail.status.isActive()) {
            throw ValidationLogicException(
                errorCode = ErrorCode.createValidation("User with email ${request.login} end sign up, please try sign in")
            )
        }

        if (!userEntity.userDetail.status.isSignUpConfirmed()) {
            throw ValidationLogicException(
                errorCode = ErrorCode.createValidation("User with email ${request.login} not confirm sign up with code, please try confirm registration")
            )
        }

        userEntity.userDetail.fio = request.fio
        userEntity.userDetail.mobileNumber = request.mobileNumber
        userEntity.userDetail.location = request.location
        userEntity.userDetail.passwordHash = passwordEncoder.encode(request.password)
        userEntity.userDetail.status = UserStatus.ACTIVE

        userService.update(userEntity)
    }

}

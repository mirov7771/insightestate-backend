package ru.nemodev.insightestate.service.security

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.api.auth.v1.dto.SignInDtoRs
import ru.nemodev.insightestate.api.auth.v1.dto.SignUpConfirmCodeDtoRq
import ru.nemodev.insightestate.api.auth.v1.dto.SignUpDtoRq
import ru.nemodev.insightestate.api.auth.v1.dto.SignUpEndDtoRq
import ru.nemodev.insightestate.config.property.AppProperties
import ru.nemodev.insightestate.entity.UserEntity
import ru.nemodev.insightestate.entity.UserStatus
import ru.nemodev.insightestate.extension.toAuthenticationToken
import ru.nemodev.insightestate.service.EmailService
import ru.nemodev.insightestate.service.UserService
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.LogicException
import ru.nemodev.platform.core.exception.logic.NotFoundLogicalException
import ru.nemodev.platform.core.exception.logic.ValidationLogicException
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

interface AuthService {
    fun signUp(request: SignUpDtoRq)
    fun signUpCheckConfirmCode(request: SignUpConfirmCodeDtoRq)
    fun signUpSendNewConfirmCode(request: SignUpDtoRq)
    fun signUpEnd(request: SignUpEndDtoRq)
    fun signIn(authBasicToken: String): SignInDtoRs
}

@Service
class AuthServiceImpl(
    private val appProperties: AppProperties,
    private val userService: UserService,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    private val confirmCodeGenerator: ConfirmCodeGenerator,
    private val authManager: AuthenticationManager,
    private val jwtEncoder: JwtEncoder
) : AuthService {

    override fun signUp(request: SignUpDtoRq) {
        val userEntity = userService.findByLogin(request.login)

        if (userEntity != null) {
            throw LogicException(
                errorCode = ErrorCode.create(HttpStatus.CONFLICT.value().toString(), "Client with email already sign up, please try sign in or send confirm registration code"),
                httpStatus = HttpStatus.CONFLICT
            )
        } else {
            val signUpConfirmCode = confirmCodeGenerator.generateDigits()

            emailService.signUpSendConfirmCode(
                email = request.login,
                confirmCode = signUpConfirmCode
            )
            userService.create(
                login = request.login,
                signUpConfirmCode = signUpConfirmCode,
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
                errorCode = ErrorCode.createValidation("User with email ${request.login} already confirmed sign up, please try sign in or end registration")
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

    override fun signIn(authBasicToken: String): SignInDtoRs {
        val authToken = authBasicToken.toAuthenticationToken()
        val authentication = authManager.authenticate(authToken)

        val userEntity = authentication.principal as UserEntity
        val accessToken = generateToken(userEntity, appProperties.auth.tokens.access.timeToLive)
        val refreshToken = generateToken(userEntity, appProperties.auth.tokens.refresh.timeToLive)

        return SignInDtoRs(
            accessToken = accessToken.tokenValue,
            tokenType = "Bearer",
            expiresIn = accessToken.expiresAt!!.epochSecond,
            refreshToken = refreshToken.tokenValue
        )
    }

    private fun generateToken(user: UserDetails, tokenLiveTme: Duration): Jwt {
        val now = Instant.now()
        val scope = user.authorities.joinToString(" ", transform = GrantedAuthority::getAuthority)
        val claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .notBefore(now)
            .expiresAt(now.plus(tokenLiveTme.toMillis(), ChronoUnit.MILLIS))
            .subject(user.username)
            .claim("scope", scope)
            .build()

        return jwtEncoder.encode(JwtEncoderParameters.from(claims))
    }

}

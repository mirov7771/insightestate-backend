package ru.nemodev.insightestate.service.security

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.service.UserService

@Service
class UserAuthenticationManager(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
) : AuthenticationManager {

    companion object {
        const val BAD_CREDENTIALS_MESSAGE = "User login or password not correct"
    }

    override fun authenticate(authentication: Authentication): Authentication {
        if ((authentication.credentials as String).isBlank()) {
            throw BadCredentialsException(BAD_CREDENTIALS_MESSAGE)
        }

        val user = userService.loadUserByUsername(authentication.name)
        if (!passwordEncoder.matches(authentication.credentials as String, user.password)) {
            throw BadCredentialsException(BAD_CREDENTIALS_MESSAGE)
        }

        return createUsernamePasswordAuthenticationToken(user)
    }

    private fun createUsernamePasswordAuthenticationToken(userDetails: UserDetails): UsernamePasswordAuthenticationToken {
        return UsernamePasswordAuthenticationToken.authenticated(
            userDetails,
            userDetails.password,
            userDetails.authorities
        )
    }
}
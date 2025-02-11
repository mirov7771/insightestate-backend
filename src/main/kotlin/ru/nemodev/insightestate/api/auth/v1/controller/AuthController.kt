package ru.nemodev.insightestate.api.auth.v1.controller

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nemodev.insightestate.api.auth.v1.dto.SignUpConfirmCodeDtoRq
import ru.nemodev.insightestate.api.auth.v1.dto.SignUpDtoRq
import ru.nemodev.insightestate.api.auth.v1.dto.SignUpEndDtoRq
import ru.nemodev.insightestate.service.AuthService

@RestController
@RequestMapping("/auth", produces = [MediaType.APPLICATION_JSON_VALUE])
class AuthController (
    private val authService: AuthService
) {
    @PostMapping("/sign-up")
    fun signUp(
        @RequestBody
        @Valid
        request: SignUpDtoRq
    ) {
        authService.signUp(request)
    }

    @PostMapping("/sign-up/confirm-code/check")
    fun signUpConfirmCodeCheck(
        @RequestBody
        @Valid
        request: SignUpConfirmCodeDtoRq
    ) {
        authService.signUpCheckConfirmCode(request)
    }

    @PostMapping("/sign-up/confirm-code/new")
    fun signUpConfirmCodeNew(
        @RequestBody
        @Valid
        request: SignUpDtoRq
    ) {
        authService.signUpSendNewConfirmCode(request)
    }

    @PostMapping("/sign-up/end")
    fun signUpEnd(
        @RequestBody
        @Valid
        request: SignUpEndDtoRq
    ) {
        authService.signUpEnd(request)
    }

    @PostMapping("/sign-in")
    fun signIn() {

    }
}

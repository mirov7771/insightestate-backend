package ru.nemodev.insightestate.api.auth.v1.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nemodev.insightestate.service.AuthService

@RestController
@RequestMapping("auth", produces = [MediaType.APPLICATION_JSON_VALUE])
class AuthController (
    private val service: AuthService
) {
}

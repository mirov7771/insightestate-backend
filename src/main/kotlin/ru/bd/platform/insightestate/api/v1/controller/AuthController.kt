package ru.bd.platform.insightestate.api.v1.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.bd.platform.insightestate.service.AuthService

@RestController
@RequestMapping("auth", produces = [MediaType.APPLICATION_JSON_VALUE])
class AuthController (
    private val service: AuthService
) {
}

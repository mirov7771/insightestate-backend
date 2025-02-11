package ru.nemodev.insightestate.api.client.v1.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nemodev.insightestate.service.UserService

@RestController
@RequestMapping("user", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController (
    private val service: UserService
) {
}

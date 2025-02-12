package ru.nemodev.insightestate.config.api

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.nemodev.platform.core.api.dto.error.ErrorDtoRs
import ru.nemodev.platform.core.api.dto.error.StatusDtoRs
import ru.nemodev.platform.core.logging.sl4j.Loggable

@RestControllerAdvice
class SecurityApiExceptionHandler {

    companion object : Loggable {
        private fun getRequestError(request: HttpServletRequest): String {
            return "Ошибка обработки запроса [method = ${request.method} path = ${request.requestURI}]"
        }
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun onBadCredentialsException(exception: BadCredentialsException, request: HttpServletRequest): ResponseEntity<ErrorDtoRs> {
        logError(exception) { getRequestError(request) }

        return ResponseEntity(
            ErrorDtoRs(
                status = StatusDtoRs(
                    code = HttpStatus.UNAUTHORIZED.name,
                    description = exception.message ?: "User login or password not correct"
                )
            ),
            HttpStatus.UNAUTHORIZED
        )
    }
}
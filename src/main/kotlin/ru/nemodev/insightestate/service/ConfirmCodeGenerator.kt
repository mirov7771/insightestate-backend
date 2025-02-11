package ru.nemodev.insightestate.service

import org.springframework.stereotype.Service
import java.security.SecureRandom

interface ConfirmCodeGenerator {
    fun generateDigits(): String
}

@Service
class ConfirmCodeGeneratorImpl : ConfirmCodeGenerator {

    companion object {
        private val secureRandom = SecureRandom()
    }

    override fun generateDigits(): String {
        return (secureRandom.nextInt(899999) + 100000).toString()
    }

}
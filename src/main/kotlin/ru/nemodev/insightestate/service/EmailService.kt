package ru.nemodev.insightestate.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import ru.nemodev.platform.core.logging.sl4j.Loggable

interface EmailService {
    fun signUpSendConfirmCode(email: String, confirmCode: String)
}

@Service
class EmailServiceImpl(
    private val emailSender: JavaMailSender
) : EmailService {

    companion object : Loggable

    override fun signUpSendConfirmCode(email: String, confirmCode: String) {
        send(
            email = email,
            subject = "Confirmation code for sign up to https://www.insightestate.com",
            message = "Your code - $confirmCode"
        )
    }

    private fun send(email: String, subject: String, message: String) {
        val simpleMailMessage = SimpleMailMessage().apply {
            this.setTo(email)
            this.subject = subject
            this.text = message
        }

        emailSender.send(simpleMailMessage)

        logInfo { "Email sent to $email\n subject - $subject\n message -$simpleMailMessage" }
    }
}
package ru.nemodev.insightestate.service

import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.config.property.AppProperties
import ru.nemodev.platform.core.logging.sl4j.Loggable

interface EmailService {
    fun signUpSendConfirmCode(email: String, confirmCode: String)
    fun sendResetPasswordCode(email: String, resetPasswordCode: String)
    fun sendToAdmin(subject: String, message: String)
    fun sendMessage(email: String, subject: String, message: String)
}

@Service
class EmailServiceImpl(
    private val emailSender: JavaMailSenderImpl,
    private val properties: AppProperties
) : EmailService {

    companion object : Loggable

    override fun signUpSendConfirmCode(email: String, confirmCode: String) {
        send(
            email = email,
            subject = "Confirmation code for sign up to https://lotsof.properties/",
            message = "Your code - $confirmCode"
        )
    }

    override fun sendResetPasswordCode(email: String, resetPasswordCode: String) {
        send(
            email = email,
            subject = "Confirmation code for reset password on https://lotsof.properties/",
            message = "Your reset code - $resetPasswordCode"
        )
    }

    private fun send(email: String, subject: String, message: String) {
        val emailMessage = emailSender.createMimeMessage()
        MimeMessageHelper(emailMessage, false).apply {
            setFrom("it@insightestate.com", "Lots Of Properties")
            setTo(email)
            setSubject(subject)
            setText(message)
        }

        emailSender.send(emailMessage)

        logInfo { "Email sent to $email\n subject - $subject\n message - $message" }
    }

    override fun sendToAdmin(subject: String, message: String) {
        properties.adminEmails.forEach {
            send(it, subject, message)
        }
    }

    override fun sendMessage(email: String, subject: String, message: String) {
        send(email, subject, message)
    }
}

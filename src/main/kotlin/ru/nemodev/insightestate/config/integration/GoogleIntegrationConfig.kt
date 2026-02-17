package ru.nemodev.insightestate.config.integration

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.apache.ApacheHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration(proxyBeanMethods = false)
class GoogleIntegrationConfig {

    @Bean
    fun googleDrive(): Drive {
        val credential = GoogleCredential.fromStream(this::class.java.getResourceAsStream("/google/credentials.json")!!)
            .createScoped(
                listOf(
                    SheetsScopes.DRIVE_READONLY,
                    DriveScopes.DRIVE,
                    DriveScopes.DRIVE_READONLY,
                )
            )

        return Drive.Builder(
            ApacheHttpTransport(),
            JacksonFactory.getDefaultInstance()
        ) { request ->
            credential.initialize(request)
            request.readTimeout = Duration.ofMinutes(3).toMillis().toInt()
        }.setApplicationName("EstateBackend")
            .build()
    }

    @Bean
    fun sheetsService(): Sheets {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()

        val credentialsStream = this::class.java.getResourceAsStream("/google/credentials.json")
            ?: throw IllegalStateException("credentials.json не найден в resources/google")

        val credential = GoogleCredential.fromStream(credentialsStream)
            .createScoped(listOf("https://www.googleapis.com/auth/spreadsheets"))

        return Sheets.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("EstateBackend")
            .build()
    }
}

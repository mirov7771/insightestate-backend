package ru.nemodev.insightestate.config.integration

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.MemoryDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import ru.nemodev.insightestate.config.property.GoogleProperties
import ru.nemodev.platform.core.integration.http.factory.RestClientFactory
import java.io.InputStreamReader

@Configuration(proxyBeanMethods = false)
class GoogleIntegrationConfig {

    @Bean
    fun googleDocsRestClient(
        googleProperties: GoogleProperties,
        restClientFactory: RestClientFactory
    ): RestClient {
        return restClientFactory.create(googleProperties.spreadsheets.integration.http)
    }

//    @Bean
    fun googleSheets(): Sheets {
        val gsonFactory = GsonFactory.getDefaultInstance()

        // TODO настройка oauth client
        val clientSecrets = GoogleClientSecrets.load(
            gsonFactory,
            InputStreamReader(this::class.java.getResourceAsStream("/google/credentials.json")!!)
        )

        val googleNetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val flow = GoogleAuthorizationCodeFlow.Builder(
            googleNetHttpTransport,
            gsonFactory,
            clientSecrets,
            listOf(SheetsScopes.SPREADSHEETS_READONLY)
        ).setDataStoreFactory(MemoryDataStoreFactory.getDefaultInstance())
            .setAccessType("offline")
            .build()

        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        val credential = AuthorizationCodeInstalledApp(flow, receiver)
            .authorize("user")

        return Sheets.Builder(googleNetHttpTransport, gsonFactory, credential)
            .setApplicationName("Estate")
            .build()
    }
}
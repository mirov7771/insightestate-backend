package ru.nemodev.insightestate.integration.google

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.io.InputStream

interface GoogleSpreadsheetsIntegration {
    fun downloadSpreadsheets(sheetId: String): InputStream
}

@Component
class GoogleSpreadsheetsIntegrationImpl(
    private val googleDocsRestClient: RestClient,
//    private val googleSheets: Sheets
) : GoogleSpreadsheetsIntegration {

    override fun downloadSpreadsheets(sheetId: String): InputStream {
//        val baos = ByteArrayOutputStream()
//        googleSheets.spreadsheets().get(sheetId).executeAndDownloadTo(baos)
//        return ByteArrayInputStream(baos.use { it.toByteArray() })

//        return googleDocsRestClient
//            .get()
//            .uri("/spreadsheets/d/{tableId}/export?format=xlsx", tableId)
//            .retrieve()
//            .body(ByteArrayResource::class.java)!!
//            .inputStream
        return InputStream.nullInputStream()
    }

}
package ru.nemodev.insightestate.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import ru.nemodev.platform.core.integration.http.config.RestClientProperties

@ConfigurationProperties("google")
data class GoogleProperties(
    val spreadsheets: Spreadsheets
) {
    data class Spreadsheets(
        val estateSheetId: String,
        val integration: Integration
    ) {
        data class Integration(
            val http: RestClientProperties
        )
    }
}

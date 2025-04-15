package ru.nemodev.insightestate.config.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("google")
data class GoogleProperties(
    val spreadsheets: Spreadsheets
) {
    data class Spreadsheets(
        val estateSpreadsheetId: String,
        val estateSheetId: Int
    )
}

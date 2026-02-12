package ru.nemodev.insightestate.integration.google

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.sheets.v4.Sheets
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import ru.nemodev.insightestate.service.estate.EstateExcelParserImpl.Companion.ESTATE_SHEET
import ru.nemodev.insightestate.service.estate.EstateExcelParserImpl.Companion.UNITS_SHEET
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


interface GoogleDriveIntegration {
    fun downloadExcelFile(fileId: String): InputStream
    fun downloadImageFiles(): List<File>
    fun downloadImageFile(fileId: String): InputStream
}

@Component
class GoogleDriveIntegrationImpl(
    private val googleDrive: Drive,
    private val sheetsService: Sheets
) : GoogleDriveIntegration {

    override fun downloadExcelFile(fileId: String): InputStream {
        val workbook = XSSFWorkbook()

        val sheetNames = listOf(ESTATE_SHEET, UNITS_SHEET)
        sheetNames.forEach { sheetName ->
            val sheet = workbook.createSheet(sheetName)

            val response = sheetsService.spreadsheets().values()
                .get(fileId, sheetName)
                .execute()

            val values: List<List<Any>> = response.getValues() ?: emptyList()

            values.forEachIndexed { rowIndex, rowValues ->
                val row = sheet.createRow(rowIndex)
                rowValues.forEachIndexed { colIndex, cellValue ->
                    val cell = row.createCell(colIndex)
                    cell.setCellValue(cellValue.toString())
                }
            }
        }

        val out = ByteArrayOutputStream()
        workbook.write(out)
        workbook.close()
        return ByteArrayInputStream(out.toByteArray())
    }

    override fun downloadImageFiles(): List<File> {
        val imageFiles = mutableListOf<File>()
        var pageToken: String? = null

        while (true) {
            val currentImageFiles = googleDrive
                .files()
                .list()
                .setSpaces("drive")
                .setQ("'1Qvr05GYGGjE4H--GDw4cavqWuwpkF2y3' in parents and mimeType='image/webp'")
                .setPageToken(pageToken)
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .execute()

            imageFiles.addAll(currentImageFiles.files)
            pageToken = currentImageFiles.nextPageToken
            if (pageToken == null || currentImageFiles.files.isEmpty()) {
                break
            }
        }

        return imageFiles
    }

    override fun downloadImageFile(fileId: String): InputStream {
        return ByteArrayOutputStream().use {
            googleDrive.files()
                .get(fileId)
                .executeMediaAndDownloadTo(it)
            ByteArrayInputStream(it.toByteArray())
        }
    }

}

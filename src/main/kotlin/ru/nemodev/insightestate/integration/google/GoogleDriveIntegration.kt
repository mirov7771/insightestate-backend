package ru.nemodev.insightestate.integration.google

import com.google.api.services.drive.Drive
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


interface GoogleDriveIntegration {
    fun downloadExcelFile(fileId: String): InputStream
}

@Component
class GoogleDriveIntegrationImpl(
    private val googleDrive: Drive
) : GoogleDriveIntegration {

    override fun downloadExcelFile(fileId: String): InputStream {
        return ByteArrayOutputStream().use {
            googleDrive.files()
                .export(fileId, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .executeMediaAndDownloadTo(it)
            ByteArrayInputStream(it.toByteArray())
        }
    }

}
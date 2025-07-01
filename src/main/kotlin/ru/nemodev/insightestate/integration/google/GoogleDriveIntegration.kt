package ru.nemodev.insightestate.integration.google

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import org.springframework.stereotype.Component
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
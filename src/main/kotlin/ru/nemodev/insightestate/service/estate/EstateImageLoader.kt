package ru.nemodev.insightestate.service.estate

import org.springframework.stereotype.Component
import ru.nemodev.insightestate.config.property.AppProperties
import ru.nemodev.platform.core.extensions.getFileExtension
import ru.nemodev.platform.core.integration.s3.minio.client.MinioS3Client
import ru.nemodev.platform.core.logging.sl4j.Loggable
import java.io.File

interface EstateImageLoader {
    fun loadFromDir()
}

@Component
class EstateImageLoaderImpl(
    private val appProperties: AppProperties,
    private val minioS3Client: MinioS3Client,
    private val estateService: EstateService
) : EstateImageLoader {

    companion object : Loggable

    private data class EstateImages(
        val projectId: String,
        val facilityImages: MutableList<EstateImage> = mutableListOf(),
        val exteriorImages: MutableList<EstateImage> = mutableListOf(),
        val interiorImages: MutableList<EstateImage> = mutableListOf()
    )

    private data class EstateImage(
        val order: Int,
        val name: String,
        val file: File,
    )

    override fun loadFromDir() {
        logInfo { "Начало загрузки фото объектов недвижимости" }

        val estates = estateService.findAll()
        if (estates.isEmpty()) {
            logWarn { "Фото объектов не могут быть загружены т.к объектов нет в базе данных" }
            return
        }

        val estateImageMap = mutableMapOf<String, EstateImages>()

        File(appProperties.estate.imageDir)
            .listFiles()
            ?.filter { it.isFile && !it.name.endsWith(".zip") }
            ?.forEach { imageFile ->
                val originalName = imageFile.name
                val originalExtension = originalName.getFileExtension()
                val projectId = originalName.substringBefore("_")
                val type = originalName.substringAfter("_").substringBefore("_")
                val order = originalName.substringAfterLast("_").substringBefore(".").filter { it.isDigit() }.toInt()
                val extension = when (originalExtension) {
                    "jpg", "JPG" -> "jpeg"
                    else -> originalExtension
                }
                val estateImages = estateImageMap.computeIfAbsent(projectId) {
                    EstateImages(
                        projectId = projectId,
                    )
                }
                val estateImage = EstateImage(
                    order = order,
                    name = "${projectId}_${type}_${order}.$extension",
                    file = imageFile,
                )
                when (type) {
                    "fac" -> estateImages.facilityImages.add(estateImage)
                    "ext" -> estateImages.exteriorImages.add(estateImage)
                    "int" -> estateImages.interiorImages.add(estateImage)
                }
            }

        if (estateImageMap.isEmpty()) {
            logWarn { "В директории ${appProperties.estate.imageDir} нет фото объектов" }
            return
        }

        estates.forEach { estate ->
            val estateImages = estateImageMap[estate.estateDetail.projectId]
            if (estateImages != null) {
                estate.estateDetail.facilityImages = estateImages.facilityImages.sortedBy { it.order }.map { it.name }.toMutableList()
                estate.estateDetail.exteriorImages = estateImages.exteriorImages.sortedBy { it.order }.map { it.name }.toMutableList()
                estate.estateDetail.interiorImages = estateImages.interiorImages.sortedBy { it.order }.map { it.name }.toMutableList()

                estateImages.facilityImages.forEach { imageFile ->
                    minioS3Client.upload(
                        fileName = imageFile.name,
                        fileContentType = "image/${imageFile.file.extension}",
                        file = imageFile.file.readBytes(),
                    )
                }
                estateImages.exteriorImages.forEach { imageFile ->
                    minioS3Client.upload(
                        fileName = imageFile.name,
                        fileContentType = "image/${imageFile.file.extension}",
                        file = imageFile.file.readBytes(),
                    )
                }
                estateImages.interiorImages.forEach { imageFile ->
                    minioS3Client.upload(
                        fileName = imageFile.name,
                        fileContentType = "image/${imageFile.file.extension}",
                        file = imageFile.file.readBytes(),
                    )
                }
            }
        }

        estateService.saveAll(estates)

        logInfo { "Закончили загрузку фото объектов недвижимости" }
    }

}
package ru.nemodev.insightestate.service.estate

import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import ru.nemodev.insightestate.config.property.AppProperties
import ru.nemodev.insightestate.integration.google.GoogleDriveIntegration
import ru.nemodev.platform.core.async.executor.CoroutineExecutor
import ru.nemodev.platform.core.extensions.getFileExtension
import ru.nemodev.platform.core.integration.s3.minio.client.MinioS3Client
import ru.nemodev.platform.core.logging.sl4j.Loggable
import java.io.File
import java.util.concurrent.locks.ReentrantLock

interface EstateImageLoader {
    fun loadFromDir()
    fun loadFromGoogleDrive()
}

@Component
class EstateImageLoaderImpl(
    private val appProperties: AppProperties,
    private val minioS3Client: MinioS3Client,
    private val estateService: EstateService,
    private val googleDriveIntegration: GoogleDriveIntegration,
    private val ioCoroutineExecutor: CoroutineExecutor,
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
        val extension: String,
        val file: File? = null,
        val googleDriveFileId: String? = null,
    )

    private val updatePhotoLock = ReentrantLock()

    override fun loadFromDir() {
        withUpdatePhotoLock {
            logInfo { "Начало загрузки фото объектов недвижимости из директории" }

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
                        extension = extension,
                        file = imageFile,
                    )
                    when (type) {
                        "fac" -> estateImages.facilityImages.add(estateImage)
                        "ext" -> estateImages.exteriorImages.add(estateImage)
                        "int" -> estateImages.interiorImages.add(estateImage)
                    }
                }

            if (estateImageMap.isEmpty()) {
                logError { "В директории ${appProperties.estate.imageDir} нет фото объектов" }
                return@withUpdatePhotoLock
            }

            loadImages(estateImageMap)

            logInfo { "Закончили загрузку фото объектов недвижимости из директории" }
        }
    }

    override fun loadFromGoogleDrive() {
        withUpdatePhotoLock {
            logInfo { "Начало загрузки фото объектов недвижимости из google drive" }

            val estateImageMap = mutableMapOf<String, EstateImages>()

            googleDriveIntegration.downloadImageFiles().forEach { imageFile ->
                try {
                    val originalName = imageFile.name
                    val originalExtension = imageFile.fileExtension ?: imageFile.name.getFileExtension()
                    val projectId = originalName.substringBefore("_")
                    val type = originalName.substringAfter("_").substringBefore("_")
                    val order = originalName.substringAfterLast("_").substringBefore(".").filter { it.isDigit() }.toInt()
                    val extension = when (originalExtension) {
                        "jpg", "JPG" -> "jpeg"
                        "PNG" -> "png"
                        else -> originalExtension
                    }.lowercase()
                    val estateImages = estateImageMap.computeIfAbsent(projectId) {
                        EstateImages(
                            projectId = projectId,
                        )
                    }
                    val estateImage = EstateImage(
                        order = order,
                        name = "${projectId}_${type}_${order}.$extension",
                        extension = extension,
                        googleDriveFileId = imageFile.id,
                    )
                    when (type) {
                        "fac" -> estateImages.facilityImages.add(estateImage)
                        "ext" -> estateImages.exteriorImages.add(estateImage)
                        "int" -> estateImages.interiorImages.add(estateImage)
                    }
                } catch (e: Exception) {
                    logError(e) { "Ошибка парсинга имени фото объекта - ${imageFile.name}" }
                }
            }

            if (estateImageMap.isEmpty()) {
                logError { "В google drive нет фото объектов" }
                return@withUpdatePhotoLock
            }

            loadImages(estateImageMap)

            logInfo { "Закончили загрузку фото объектов недвижимости из google drive" }
        }
    }

    private fun loadImages(estateImageMap: Map<String, EstateImages>) {
        val estates = estateService.findAll()
        if (estates.isEmpty()) {
            logError { "Фото объектов не могут быть загружены т.к объектов нет в базе данных" }
            return
        }

        val estateTasks = estates.map { estate ->
            ioCoroutineExecutor.async {
                val estateImages = estateImageMap[estate.estateDetail.projectId]
                if (estateImages != null) {

                    val facilityImagesTask = ioCoroutineExecutor.async {
                        loadImageToMinio(estateImages.facilityImages)
                    }

                    val exteriorImagesTask = ioCoroutineExecutor.async {
                        loadImageToMinio(estateImages.exteriorImages)
                    }

                    val interiorImagesTask = ioCoroutineExecutor.async {
                        loadImageToMinio(estateImages.interiorImages)
                    }

                    // Одновременная загрузка всех картинок объекта
                    runBlocking { listOf(facilityImagesTask, exteriorImagesTask, interiorImagesTask).awaitAll() }

                    // Обновляем фото объекта
                    estate.estateDetail.facilityImages = estateImages.facilityImages.sortedBy { it.order }.map { it.name }.toMutableList()
                    estate.estateDetail.exteriorImages = estateImages.exteriorImages.sortedBy { it.order }.map { it.name }.toMutableList()
                    estate.estateDetail.interiorImages = estateImages.interiorImages.sortedBy { it.order }.map { it.name }.toMutableList()
                }

                // Обновляем признак можно ли показывать объект
                estate.estateDetail.canShow = estate.isCanShow()
            }
        }

        // Одновременная загрузка фото всех объектов
        runBlocking { estateTasks.awaitAll() }

        estateService.saveAll(estates)
    }

    private fun withUpdatePhotoLock(action: () -> Unit) {
        if (updatePhotoLock.tryLock()) {
            try {
                action()
            } finally {
                updatePhotoLock.unlock()
            }
        } else {
            logInfo { "Загрузка фото объектов уже запущена, дождитесь окончания текущей загрузки" }
        }
    }

    private fun loadImageToMinio(estateImages: List<EstateImage>) {
        estateImages.forEach { imageFile ->
            try {
                val imageSource = imageFile.file?.readBytes()
                    ?: googleDriveIntegration.downloadImageFile(imageFile.googleDriveFileId!!).readAllBytes()
                minioS3Client.upload(
                    fileName = imageFile.name,
                    fileContentType = "image/${imageFile.extension}",
                    file = imageSource,
                )
            } catch (e: Exception) {
                logError(e) { "Ошибка загрузки фото объекта - ${imageFile.name}" }
            }
        }
    }

}
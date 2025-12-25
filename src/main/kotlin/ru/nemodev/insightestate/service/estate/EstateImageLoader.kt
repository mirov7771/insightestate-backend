package ru.nemodev.insightestate.service.estate

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import ru.nemodev.insightestate.config.property.AppProperties
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
    private val transactionTemplate: TransactionTemplate
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

            loadImages()

            logInfo { "Закончили загрузку фото объектов недвижимости из директории" }
        }
    }

    override fun loadFromGoogleDrive() {
        withUpdatePhotoLock {
            logInfo { "Начало загрузки фото объектов недвижимости из google drive" }

            loadImages()

            logInfo { "Закончили загрузку фото объектов недвижимости из google drive" }
        }
    }

    private fun loadImages() {
        val estates = estateService.findAll().filter { !it.isCanShow() }
        if (estates.isEmpty()) {
            logError { "Фото объектов не могут быть загружены т.к объектов нет в базе данных" }
            return
        }

        estates.forEachIndexed { index, estate ->
            logInfo { "${index + 1}/${estates.size} - старт загрузки фото объекта ${estate.estateDetail.projectId}, ${estate.id}" }

            val projectId = estate.estateDetail.projectId

            val facList = mutableListOf<String>()
            val extList = mutableListOf<String>()
            val intList = mutableListOf<String>()

            for (i in 1..30) {
                val facName = "${projectId}_fac_$i.webp"
                val intName = "${projectId}_int_$i.webp"
                val extName = "${projectId}_ext_$i.webp"
                val facSize = try {
                    minioS3Client.fileParams(bucket = "estate-images", fileName = facName).size()
                } catch (_: Exception) {
                    0L
                }
                val intSize = try {
                    minioS3Client.fileParams(bucket = "estate-images", fileName = intName).size()
                } catch (_: Exception) {
                    0L
                }
                val extSize = try {
                    minioS3Client.fileParams(bucket = "estate-images", fileName = extName).size()
                } catch (_: Exception) {
                    0L
                }
                if (facSize > 0) {
                    facList.add(facName)
                }
                if (extSize > 0) {
                    extList.add(extName)
                }
                if (intSize > 0) {
                    intList.add(intName)
                }
            }

            if (facList.isNotEmpty()) {
                estate.estateDetail.canShow = estate.isCanShow()
                estate.estateDetail.facilityImages = facList
            }

            if (extList.isNotEmpty()) {
                estate.estateDetail.canShow = estate.isCanShow()
                estate.estateDetail.exteriorImages = extList
            }

            if (intList.isNotEmpty()) {
                estate.estateDetail.canShow = estate.isCanShow()
                estate.estateDetail.interiorImages = intList
            }

        }

        transactionTemplate.executeWithoutResult {
            estateService.saveAll(estates)
        }
    }

    private fun withUpdatePhotoLock(action: () -> Unit) {
        if (updatePhotoLock.tryLock()) {
            try {
                action()
            } finally {
                updatePhotoLock.unlock()
            }
        } else {
            logWarn { "Загрузка фото объектов уже запущена, дождитесь окончания текущей загрузки" }
        }
    }

    //@PostConstruct
    fun loadPresentation() {
        val estates = estateService.findAll().filter { !it.estateDetail.engPresentation || !it.estateDetail.rusPresentation }
        if (estates.isEmpty()) {
            return
        }

        estates.forEachIndexed { index, estate ->
            val engName = "${estate.estateDetail.projectId}_ENG.pdf"
            val rusName = "${estate.estateDetail.projectId}_RUS.pdf"
            val engSize = try {
                minioS3Client.fileParams(bucket = "estate-images", fileName = engName).size()
            } catch (_: Exception) {
                0L
            }
            val resSize = try {
                minioS3Client.fileParams(bucket = "estate-images", fileName = rusName).size()
            } catch (_: Exception) {
                0L
            }
            val engPresentation = engSize > 0
            val rusPresentation = resSize > 0
            logInfo { "$index/${estates.size} - ENG $engPresentation , RUS $rusPresentation" }
            estate.estateDetail.rusPresentation = resSize > 0
            estate.estateDetail.engPresentation = engPresentation
        }
        estateService.saveAll(estates)
    }

}

package ru.nemodev.insightestate.service.estate

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.nemodev.insightestate.config.property.GoogleProperties
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.insightestate.integration.google.GoogleDriveIntegration
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.ValidationLogicException
import ru.nemodev.platform.core.extensions.getFileExtension
import ru.nemodev.platform.core.logging.sl4j.Loggable

interface EstateLoader {
    fun loadFromFile(filePart: MultipartFile)
    fun loadFromGoogleSpreadsheets()
}

@Service
class EstateLoaderImpl(
    private val estateExcelParser: EstateExcelParser,
    private val estateService: EstateService,
    private val googleProperties: GoogleProperties,
    private val googleDriveIntegration: GoogleDriveIntegration
) : EstateLoader {

    companion object : Loggable

    override fun loadFromFile(filePart: MultipartFile) {
        if (filePart.originalFilename?.getFileExtension() != "xlsx") {
            throw ValidationLogicException(
                errorCode = ErrorCode.createValidation("Файл объектов должен быть в формате xlsx")
            )
        }

        logInfo { "Начало парсинга объектов недвижимости из файла ${filePart.originalFilename}" }

        val parsedEstates = estateExcelParser.parse(filePart.inputStream)

        logInfo { "Закончили парсинг объектов недвижимости из файла ${filePart.originalFilename}, всего объектов - ${parsedEstates.size}" }

        load(parsedEstates)
    }

    override fun loadFromGoogleSpreadsheets() {
        logInfo { "Начало парсинга объектов недвижимости из google spreadsheets ${googleProperties.spreadsheets.estateSpreadsheetId}" }

        val driveExcelFile = googleDriveIntegration.downloadExcelFile(googleProperties.spreadsheets.estateSpreadsheetId)
        val parsedEstates = estateExcelParser.parse(driveExcelFile)

        logInfo { "Закончили парсинг объектов недвижимости из google spreadsheets ${googleProperties.spreadsheets.estateSpreadsheetId}, всего объектов - ${parsedEstates.size}" }

        load(parsedEstates)
    }

    private fun load(parsedEstates: List<EstateEntity>) {
        logInfo { "Начало обновления и загрузки новых объектов недвижимости" }

        val existsEstateByProjectMap = estateService.findAll().associateBy { it.estateDetail.projectId }.toMutableMap()
        val newEstates = mutableListOf<EstateEntity>()
        parsedEstates.forEach { parsedEstate ->
            val existEstate = existsEstateByProjectMap[parsedEstate.estateDetail.projectId]
            if (existEstate == null) {
                newEstates.add(parsedEstate)
            } else {
                // сохраняем ранее загруженные картинки
                parsedEstate.estateDetail.facilityImages = existEstate.estateDetail.facilityImages
                parsedEstate.estateDetail.exteriorImages = existEstate.estateDetail.exteriorImages
                parsedEstate.estateDetail.interiorImages = existEstate.estateDetail.interiorImages
                // обновляем информацию по объекту
                existEstate.estateDetail = parsedEstate.estateDetail
            }
        }

        val existEstates = (existsEstateByProjectMap.values.toList())

        // Скрываем объекты без фото
        newEstates.forEach {
            it.estateDetail.canShow = it.isCanShow()
        }
        existEstates.forEach {
            it.estateDetail.canShow = it.isCanShow()
        }

        estateService.saveAll(newEstates)
        estateService.saveAll(existEstates)

        logInfo { "Закончили обновление и загрузку новых объектов недвижимости" }
    }

}
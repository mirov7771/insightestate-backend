package ru.nemodev.insightestate.service.estate

import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.multipart.MultipartFile
import ru.nemodev.insightestate.config.property.GoogleProperties
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.insightestate.integration.google.GoogleDriveIntegration
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.ValidationLogicException
import ru.nemodev.platform.core.extensions.getFileExtension
import ru.nemodev.platform.core.logging.sl4j.Loggable
import java.util.concurrent.locks.ReentrantLock

interface EstateLoader {
    fun loadFromFile(filePart: MultipartFile)
    fun loadFromGoogleSpreadsheets()
}

@Service
class EstateLoaderImpl(
    private val estateExcelParser: EstateExcelParser,
    private val estateService: EstateService,
    private val googleProperties: GoogleProperties,
    private val googleDriveIntegration: GoogleDriveIntegration,
    private val transactionTemplate: TransactionTemplate,
) : EstateLoader {

    companion object : Loggable

    private val updateLock = ReentrantLock()

    override fun loadFromFile(filePart: MultipartFile) {
        withUpdateLock {
            if (filePart.originalFilename?.getFileExtension() != "xlsx") {
                throw ValidationLogicException(
                    errorCode = ErrorCode.createValidation("Файл объектов должен быть в формате xlsx")
                )
            }

            logInfo { "Начало парсинга объектов недвижимости из файла ${filePart.originalFilename}" }

            val parsedEstates = estateExcelParser.parse(filePart.inputStream)
            load(parsedEstates)

            logInfo { "Закончили парсинг и загрузку объектов недвижимости из файла ${filePart.originalFilename}, всего объектов - ${parsedEstates.size}" }
        }
    }

    override fun loadFromGoogleSpreadsheets() {
        withUpdateLock {
            logInfo { "Начало парсинга объектов недвижимости из google spreadsheets ${googleProperties.spreadsheets.estateSpreadsheetId}" }

            val driveExcelFile = googleDriveIntegration.downloadExcelFile(googleProperties.spreadsheets.estateSpreadsheetId)
            val parsedEstates = estateExcelParser.parse(driveExcelFile)

            load(parsedEstates)

            logInfo { "Закончили парсинг и загрузку объектов недвижимости из google spreadsheets ${googleProperties.spreadsheets.estateSpreadsheetId}, всего объектов - ${parsedEstates.size}" }
        }
    }

    private fun load(parsedEstates: List<EstateEntity>) {
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

        transactionTemplate.executeWithoutResult {
            estateService.saveAll(newEstates)
            estateService.saveAll(existEstates)
        }
    }

    private fun withUpdateLock(action: () -> Unit) {
        if (updateLock.tryLock()) {
            try {
                action()
            } finally {
                updateLock.unlock()
            }
        } else {
            logWarn { "Загрузка объектов уже запущена, дождитесь окончания текущей загрузки" }
        }
    }

}
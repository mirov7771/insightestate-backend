package ru.nemodev.insightestate.service.estate

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.nemodev.insightestate.repository.EstateRepository
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.ValidationLogicException
import ru.nemodev.platform.core.extensions.getFileExtension
import ru.nemodev.platform.core.logging.sl4j.Loggable
import java.io.InputStream

interface EstateLoader {
    fun loadFromFileInternal()
    fun loadFromFile(filePart: MultipartFile)
}

@Service
class EstateLoaderImpl(
    private val estateExcelParser: EstateExcelParser,
    private val estateRepository: EstateRepository,
) : EstateLoader {

    companion object : Loggable

    override fun loadFromFileInternal() {
        load(ClassPathResource("data/estate.xlsx").inputStream)
    }

    override fun loadFromFile(filePart: MultipartFile) {
        if (filePart.originalFilename?.getFileExtension() != "xlsx") {
            throw ValidationLogicException(
                errorCode = ErrorCode.createValidation("Файл объектов должен быть в формате xlsx")
            )
        }

        load(filePart.inputStream)
    }

    private fun load(inputStream: InputStream) {
        logInfo { "Начало парсинга объектов недвижимости" }
        val estates = estateExcelParser.parse(inputStream)
        logInfo { "Закончили парсинг объектов недвижимости всего - ${estates.size}" }

        logInfo { "Начало удаления и загрузки новых объектов недвижимости" }
        estateRepository.deleteAll()
        estateRepository.saveAll(estates)
        logInfo { "Закончили удаление и загрузку новых объектов недвижимости" }
    }

}
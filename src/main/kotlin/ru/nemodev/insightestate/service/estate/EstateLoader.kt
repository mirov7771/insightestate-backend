package ru.nemodev.insightestate.service.estate

import org.springframework.stereotype.Service
import ru.nemodev.insightestate.repository.EstateRepository
import ru.nemodev.platform.core.logging.sl4j.Loggable

interface EstateLoader {
    fun loadFromFile()
}

@Service
class EstateLoaderImpl(
    private val estateExcelParser: EstateExcelParser,
    private val estateRepository: EstateRepository,
) : EstateLoader {

    companion object : Loggable

    override fun loadFromFile() {
        logInfo { "Начало парсинга объектов недвижимости" }
        val estates = estateExcelParser.parse()
        logInfo { "Закончили парсинг объектов недвижимости всего - ${estates.size}" }

        logInfo { "Начало удаления и загрузки новых объектов недвижимости" }
        estateRepository.deleteAll()
        estateRepository.saveAll(estates)
        logInfo { "Закончили удаление и загрузку новых объектов недвижимости" }
    }

}
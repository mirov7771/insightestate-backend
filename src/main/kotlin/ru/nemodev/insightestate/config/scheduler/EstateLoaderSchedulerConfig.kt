package ru.nemodev.insightestate.config.scheduler

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import ru.nemodev.insightestate.service.airtable.AirtableService
import ru.nemodev.insightestate.service.estate.EstateImageLoader
import ru.nemodev.insightestate.service.estate.EstateLoader
import ru.nemodev.platform.core.logging.sl4j.Loggable

@Configuration(proxyBeanMethods = false)
@EnableScheduling
class EstateLoaderSchedulerConfig(
    private val estateLoader: EstateLoader,
    private val estateImageLoader: EstateImageLoader,
    private val airtableService: AirtableService
) {

    companion object : Loggable

    //@PostConstruct
//    @Scheduled(cron = "\${google.spreadsheets.estate-load-cron}")
    fun loadEstateFromGoogleSpreadsheets() {
        estateLoader.loadFromGoogleSpreadsheets()
    }

    //    @Scheduled(cron = "\${google.drive.estate-image-load-cron}")
    fun loadEstateImageFromGoogleDrive() {
        estateImageLoader.loadFromGoogleDrive()
    }

    //        @Scheduled(cron = "\${airtable.estate-refresh-cron}")
    fun refreshEstateAirtable() {
        estateLoader.loadFromAirtable()
        airtableService.deleteFromAirtable()
    }
}

package ru.nemodev.insightestate.config.scheduler

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import ru.nemodev.insightestate.service.estate.EstateImageLoader
import ru.nemodev.insightestate.service.estate.EstateLoader

@Configuration(proxyBeanMethods = false)
@EnableScheduling
class EstateLoaderSchedulerConfig(
    private val estateLoader: EstateLoader,
    private val estateImageLoader: EstateImageLoader
) {
    //@PostConstruct
//    @Scheduled(cron = "\${google.spreadsheets.estate-load-cron}")
    fun loadEstateFromGoogleSpreadsheets() {
        estateLoader.loadFromGoogleSpreadsheets()
    }

//    @Scheduled(cron = "\${google.drive.estate-image-load-cron}")
    fun loadEstateImageFromGoogleDrive() {
        estateImageLoader.loadFromGoogleDrive()
    }

//        @Scheduled(cron = "\${google.drive.estate-image-load-cron}")
    fun loadEstateAirtable() {
    estateLoader.loadFromAirtable()
    }
}

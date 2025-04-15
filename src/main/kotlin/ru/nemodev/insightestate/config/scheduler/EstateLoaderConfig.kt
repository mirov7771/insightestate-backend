package ru.nemodev.insightestate.config.scheduler

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import ru.nemodev.insightestate.service.estate.EstateLoader

@Configuration(proxyBeanMethods = false)
@EnableScheduling
class EstateLoaderConfig(
    private val estateLoader: EstateLoader
) {
    @Scheduled(cron = "\${google.spreadsheets.estate-load-cron}")
    fun loadEstateFromGoogleSpreadsheets() {
        estateLoader.loadFromGoogleSpreadsheets()
    }
}
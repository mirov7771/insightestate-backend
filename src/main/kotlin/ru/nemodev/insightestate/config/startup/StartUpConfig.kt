package ru.nemodev.insightestate.config.startup

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import ru.nemodev.insightestate.service.estate.EstateLoader

@Configuration(proxyBeanMethods = false)
class StartUpConfig(
    private val estateLoader: EstateLoader
) {
    @EventListener(ApplicationReadyEvent::class)
    fun onStartUp() {
        estateLoader.loadFromFile()
    }
}
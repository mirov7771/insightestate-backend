package ru.nemodev.insightestate.config.integration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.nemodev.platform.core.integration.http.config.RestClientProperties
import ru.nemodev.platform.core.integration.http.factory.RestClientFactory

@ConfigurationProperties("cutt")
class CuttProperties (
    val integration: CuttIntegration
) {
    data class CuttIntegration (
        val httpClient: RestClientProperties
    )
}

@Configuration(proxyBeanMethods = false)
class CuttIntegrationConfig {
    @Bean
    fun cuttRestClient(
        properties: CuttProperties,
        restClientFactory: RestClientFactory
    ) = restClientFactory.create(properties.integration.httpClient)
}

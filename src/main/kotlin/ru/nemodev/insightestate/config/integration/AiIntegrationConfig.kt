package ru.nemodev.insightestate.config.integration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.nemodev.platform.core.integration.http.config.RestClientProperties
import ru.nemodev.platform.core.integration.http.factory.RestClientFactory

@ConfigurationProperties("ai")
class AiProperties (
    val integration: AiIntegration
) {
    data class AiIntegration (
        val httpClient: RestClientProperties
    )
}

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(value = [AiProperties::class])
class AiIntegrationConfig {
    @Bean
    fun aiRestClient(
        properties: AiProperties,
        restClientFactory: RestClientFactory
    ) = restClientFactory.create(properties.integration.httpClient)
}

package ru.nemodev.insightestate.config.integration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.nemodev.platform.core.integration.http.config.RestClientProperties
import ru.nemodev.platform.core.integration.http.factory.RestClientFactory

@ConfigurationProperties("gpt")
class GptProperties (
    val integration: GptIntegration
) {
    data class GptIntegration (
        val httpClient: RestClientProperties
    )
}

@Configuration(proxyBeanMethods = false)
class GptIntegrationConfig {
    @Bean
    fun gptRestClient(
        properties: GptProperties,
        restClientFactory: RestClientFactory
    ) = restClientFactory.create(properties.integration.httpClient)
}

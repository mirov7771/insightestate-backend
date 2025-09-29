package ru.nemodev.insightestate.config.integration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.nemodev.platform.core.integration.http.config.RestClientProperties
import ru.nemodev.platform.core.integration.http.factory.RestClientFactory

@ConfigurationProperties("currency")
class CurrencyProperties (
    val integration: CurrencyIntegration
) {
    data class CurrencyIntegration (
        val httpClient: RestClientProperties
    )
}

@Configuration(proxyBeanMethods = false)
class CurrencyIntegrationConfig {
    @Bean
    fun currencyRestClient(
        properties: CurrencyProperties,
        restClientFactory: RestClientFactory
    ) = restClientFactory.create(properties.integration.httpClient)
}

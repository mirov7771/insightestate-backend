package ru.nemodev.insightestate.config.integration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.nemodev.insightestate.entity.Country
import ru.nemodev.platform.core.integration.http.config.RestClientProperties
import ru.nemodev.platform.core.integration.http.factory.RestClientFactory

@ConfigurationProperties("airtable")
class AirtableProperties (
    val integration: AirtableIntegration,
    val markets: Map<Country, Market>
) {
    data class AirtableIntegration (
        val httpClient: RestClientProperties
    )

    data class Market(
        val baseId: String,
        val estateTableId: String,
        val unitsTableId: String
    )
}

@Configuration(proxyBeanMethods = false)
class AirtableIntegrationConfig {
    @Bean
    fun airtableRestClient(
        properties: AirtableProperties,
        restClientFactory: RestClientFactory
    ) = restClientFactory.create(properties.integration.httpClient)
}

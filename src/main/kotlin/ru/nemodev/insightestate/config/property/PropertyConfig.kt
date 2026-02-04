package ru.nemodev.insightestate.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import ru.nemodev.insightestate.config.integration.CurrencyProperties
import ru.nemodev.insightestate.config.integration.CuttProperties
import ru.nemodev.insightestate.config.integration.GptProperties
import java.time.Duration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(
    AppProperties::class,
    GoogleProperties::class,
    GptProperties::class,
    CurrencyProperties::class,
    CuttProperties::class,
)
class PropertyConfig

// TODO вынести в отдельный файл
@ConfigurationProperties("insightestate")
data class AppProperties(
    val auth: Auth,
    val imageBaseUrl: String,
    val estate: Estate,
    val developerEmail: String,
) {
    data class Auth(
        val tokens: AuthTokens
    ) {
        data class AuthTokens(
            val rsaPrivateKey: String,
            val access: AuthToken,
            val refresh: AuthToken,
        ) {
            data class AuthToken(
                val timeToLive: Duration
            )
        }
    }

    data class Estate(
        val imageDir: String
    )
}

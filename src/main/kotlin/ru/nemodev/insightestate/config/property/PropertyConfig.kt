package ru.nemodev.insightestate.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(
    AppProperties::class,
    GoogleProperties::class
)
class PropertyConfig

// TODO вынести в отдельный файл
@ConfigurationProperties("insightestate")
data class AppProperties(
    val auth: Auth,
    val imageBaseUrl: String,
    val estate: Estate
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
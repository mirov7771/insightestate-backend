package ru.nemodev.insightestate.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AppProperties::class)
class PropertyConfig

@ConfigurationProperties("insightestate")
data class AppProperties(
    val auth: Auth,
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
}
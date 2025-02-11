package ru.nemodev.insightestate.config.security

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import ru.nemodev.insightestate.config.property.AppProperties
import ru.nemodev.platform.core.extensions.toRSAPrivateKey
import ru.nemodev.platform.core.extensions.toRSAPublicKey

@Configuration(proxyBeanMethods = false)
class OAuth2ServerConfig(
    private val appProperties: AppProperties
) {
    @Bean
    fun jwkSource(
        @Value("\${platform.security.oauth2-resource.rsa-key.public-key}")
        rsaPublicKey: String
    ): JWKSource<SecurityContext> {
        val rsaPrivateKey = appProperties.auth.tokens.rsaPrivateKey.toRSAPrivateKey()
        val rsaKey = RSAKey.Builder(rsaPublicKey.toRSAPublicKey())
            .privateKey(rsaPrivateKey)
            .keyID("auth-key")
            .build()

        return ImmutableJWKSet(JWKSet(rsaKey))
    }

    @Bean
    fun jwtEncoder(jwkSource: JWKSource<SecurityContext>) = NimbusJwtEncoder(jwkSource)

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}
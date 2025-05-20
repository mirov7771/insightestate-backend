package ru.nemodev.insightestate.integration.cutt

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import ru.nemodev.platform.core.logging.sl4j.Loggable

interface CuttIntegration {
    fun short(url: String): String?
}

@Component
class CuttIntegrationImpl (
    private val cuttRestClient: RestClient
) : CuttIntegration {

    companion object: Loggable

    override fun short(url: String): String? = try {
        cuttRestClient.get()
            .uri("/api-create.php") {
                it.queryParam("url", url)
                    .build()
            }
            .retrieve()
            .toEntity(String::class.java)
            .body
    } catch (e: Exception) {
        logger.error("Error while creating short link", e)
        null
    }
}

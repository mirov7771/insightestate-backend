package ru.nemodev.insightestate.integration.airtable

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import ru.nemodev.insightestate.config.integration.AirtableProperties
import ru.nemodev.insightestate.integration.airtable.dto.EstateRecordsDtoRs
import ru.nemodev.insightestate.integration.airtable.dto.UnitRecordsDtoRs
import ru.nemodev.platform.core.exception.critical.IntegrationCriticalException
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.IntegrationLogicException
import java.time.LocalDateTime


interface AirtableIntegration {
    fun estateRecords(
        updatedAt: LocalDateTime,
        pageSize: Int? = null,
        offset: String? = null
    ): EstateRecordsDtoRs

    fun unitRecords(
        updatedAt: LocalDateTime,
        pageSize: Int? = null,
        offset: String? = null
    ): UnitRecordsDtoRs
}

@Component
class AirtableIntegrationImpl(
    private val airtableProperties: AirtableProperties,
    private val airtableRestClient: RestClient
) : AirtableIntegration {

    companion object {
        private const val AIRTABLE_BASE_PATH = "/v0"
        private const val AIRTABLE_LIST_RECORDS_PATH = "$AIRTABLE_BASE_PATH/{baseId}/{tableId}"

        private const val AIRTABLE_INTEGRATION_ERROR_CODE = "AIRTABLE_INTEGRATION_ERROR_CODE"
        private const val AIRTABLE_INTEGRATION_ERROR_DESCRIPTION = "Ошибка интеграции с airtable"
    }

    override fun estateRecords(
        updatedAt: LocalDateTime,
        pageSize: Int?,
        offset: String?
    ): EstateRecordsDtoRs {
        return airtableRestClient
            .get()
            .uri(AIRTABLE_LIST_RECORDS_PATH) { uriBuilder ->
                pageSize?.let { uriBuilder.queryParam("pageSize", it) }
                offset?.let { uriBuilder.queryParam("offset", it) }
                uriBuilder.queryParam("filterByFormula", buildFilterByFormula(updatedAt))

                uriBuilder.build(
                    airtableProperties.settings.baseId,
                    airtableProperties.settings.estateTableId
                )
            }
            .retrieve()
            .onStatus(
                { it.is4xxClientError },
                { _, response ->
                    throw IntegrationLogicException(
                        serviceId = airtableProperties.integration.httpClient.serviceId,
                        errorCode = ErrorCode.create(AIRTABLE_INTEGRATION_ERROR_CODE, AIRTABLE_INTEGRATION_ERROR_DESCRIPTION),
                        httpStatus = response.statusCode
                    )
                }
            )
            .onStatus(
                { it.is5xxServerError },
                { _, response ->
                    throw IntegrationCriticalException(
                        serviceId = airtableProperties.integration.httpClient.serviceId,
                        errorCode = ErrorCode.create(AIRTABLE_INTEGRATION_ERROR_CODE, AIRTABLE_INTEGRATION_ERROR_DESCRIPTION),
                        httpStatus = response.statusCode
                    )
                }
            )
            .toEntity(EstateRecordsDtoRs::class.java)
            .body!!
    }

    override fun unitRecords(
        updatedAt: LocalDateTime,
        pageSize: Int?,
        offset: String?
    ): UnitRecordsDtoRs {
        return airtableRestClient
            .get()
            .uri(AIRTABLE_LIST_RECORDS_PATH) { uriBuilder ->
                pageSize?.let { uriBuilder.queryParam("pageSize", it) }
                offset?.let { uriBuilder.queryParam("offset", it) }
                uriBuilder.queryParam("filterByFormula", buildFilterByFormula(updatedAt))

                uriBuilder.build(
                    airtableProperties.settings.baseId,
                    airtableProperties.settings.unitsTableId
                )
            }
            .retrieve()
            .onStatus(
                { it.is4xxClientError },
                { _, response ->
                    throw IntegrationLogicException(
                        serviceId = airtableProperties.integration.httpClient.serviceId,
                        errorCode = ErrorCode.create(AIRTABLE_INTEGRATION_ERROR_CODE, AIRTABLE_INTEGRATION_ERROR_DESCRIPTION),
                        httpStatus = response.statusCode
                    )
                }
            )
            .onStatus(
                { it.is5xxServerError },
                { _, response ->
                    throw IntegrationCriticalException(
                        serviceId = airtableProperties.integration.httpClient.serviceId,
                        errorCode = ErrorCode.create(AIRTABLE_INTEGRATION_ERROR_CODE, AIRTABLE_INTEGRATION_ERROR_DESCRIPTION),
                        httpStatus = response.statusCode
                    )
                }
            )
            .toEntity(UnitRecordsDtoRs::class.java)
            .body!!
    }

    private fun buildFilterByFormula(updatedAt: LocalDateTime): String {
        return "AND(updatedAt > '${updatedAt}', active)"
    }
}

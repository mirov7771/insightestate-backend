package ru.nemodev.insightestate.integration.currency

import jakarta.annotation.PostConstruct
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import ru.nemodev.insightestate.integration.currency.dto.CurrencyDto
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

interface CurrencyService {
    fun getValueByCurrency(
        value: BigDecimal,
        currency: String
    ): BigDecimal

    fun getRate(currency: String): BigDecimal
}

@Service
class CurrencyServiceImpl (
    private val currencyRestClient: RestClient,
) : CurrencyService {

    companion object {
        private const val USD = "USD"
        private const val RUB = "RUB"
        private const val THB = "THB"
        private const val AUD = "AUD"
        private const val ILS = "ILS"
        private const val PLN = "PLN"
        private const val GBP = "GBP"
        private const val DEFAULT_RUB = "85"
        private const val DEFAULT_USD = "0.033"
        private const val DEFAULT_AUD = "0.05"
        private const val DEFAULT_ILS = "0.10"
        private const val DEFAULT_PLN = "0.12"
        private const val DEFAULT_GBP = "0.02"
    }

    private val currencyMap: ConcurrentHashMap<String, BigDecimal> = ConcurrentHashMap()

    override fun getValueByCurrency(
        value: BigDecimal,
        currency: String
    ): BigDecimal {
        if (currency == THB)
            return value
        val rate = getRate(currency)
        return value.multiply(rate)
    }

    override fun getRate(currency: String): BigDecimal {
        return currencyMap[currency]!!
    }


    @Scheduled(cron = "0 0 0 * * *")
    @PostConstruct
    fun getRates() {
        val rates = try {
            currencyRestClient.get().retrieve()
                .toEntity(CurrencyDto::class.java)
                .body?.rates
        } catch (_: Exception) {
            null
        }
        currencyMap[RUB] = rates?.rub ?: DEFAULT_RUB.toBigDecimal()
        currencyMap[USD] = rates?.usd ?: DEFAULT_USD.toBigDecimal()
        currencyMap[AUD] = rates?.aud ?: DEFAULT_AUD.toBigDecimal()
        currencyMap[ILS] = rates?.ils ?: DEFAULT_ILS.toBigDecimal()
        currencyMap[PLN] = rates?.pln ?: DEFAULT_PLN.toBigDecimal()
        currencyMap[GBP] = rates?.gbp ?: DEFAULT_GBP.toBigDecimal()
    }
}

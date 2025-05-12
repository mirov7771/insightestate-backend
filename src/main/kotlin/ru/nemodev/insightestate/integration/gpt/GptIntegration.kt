package ru.nemodev.insightestate.integration.gpt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import ru.nemodev.insightestate.integration.ai.dto.*
import ru.nemodev.platform.core.logging.sl4j.Loggable

interface GptIntegration {
    fun generate(rq: String): ResultDto
}

@Component
class GptIntegrationImpl (
    private val gptRestClient: RestClient
) : GptIntegration {

    companion object: Loggable {
        private const val PROPMT = "Обработай запрос и верни ответ в формате JSON из текста, указанного в “Текст”. JSON должен быть вложен в параметр 'result'. Типы данных и Схема JSON-ответа: “{ “type“: “string“, “rooms“: “number“, “beach“: “bool“, “gym“:“bool“,“shop“:“bool“,“childRoom“:“bool“,parking:“bool“,“beachTravelTimesCar“:“number“,“beachTravelTimesWalk“:“number“,“beachTravelTimes“:“number“,“airportTravelTimes“:“number“,“priceFrom“:“number“,“priceTo“:“number“,“currency“:“string“,“buildEndYears“:“number“}“.  Типы данных для параметров - булевый: gym, shop, childRoom, beach, parking, числовой: beachTravelTimesCar, beachTravelTimesWalk, beachTravelTimes, priceFrom, priceTo, rooms. Если указывается: Тип - вилла, то “type“= “VILLA“, апартаменты, то “type“= “APARTMENT“, иначе “type“= NULL. Количество спален - параметр “rooms“. Зал - “gym“ = true, иначе - false. Магазин или магазины - “shop“ = true, иначе - false. Детская комната - “childRoom“ = true, иначе - false. Коворкинг - “coworking“  = true, иначе - false. Пляж - “beach“  = true, иначе - false. Паркинг или парковка - “parking“  = true, иначе - false. Пляж -  “beachName“ - одно из подходящих значений: Kata,Mai Khao,Layan,Bang Tao,Rawai,Kamala,Naithon,Karon,Surin,Nai Yang,Ao Yon, иначе - NULL. Время до пляжа на машине - “beachTravelTimesCar“. Время до пляжа пешком - “beachTravelTimesWalk“, если не понятно как добраться до пляжа - “beachTravelTimes“. Район -“district“ - одно из подходящих значений: Phuket Town, Kathu,Thalang, иначе - NULL. Время до аэропорта - “airportTravelTimes“. Цена от - “priceFrom“. Цена до - “priceTo“. “Цена от“ и “Цена до“ необходимо указывать валюту, параметр “currency“, если в тексте будет указано “рублей“, то “currency“ =“RUB“, если в тексте “бат“, то “currency“ = “THB“, если ничего не указано, то “currency“=“USD“, иначе “currency“=“USD“. Дата окончания строительства - “buildEndYears“ в формате “YYYY“. Текст:“%s“"
    }

    override fun generate(rq: String): ResultDto {
        val gptRq = GptRq(
            messages = listOf(
                GptMessage(content = PROPMT.format(rq)),
                GptMessage(content = PROPMT.format(rq))
            )
        )
        val rs = callApi(gptRq) ?: return ResultDto()
        var content = rs.choices?.get(0)?.message?.content ?: return ResultDto()
        content = content.replace("Processes the request", "", ignoreCase = true)
            .replace("*", "")
            .replace("`", "")
            .replace("json", "", ignoreCase = true)
            .replace("\\n", "")
            .replace("\\", "")
            .replace("обрабатывает запрос", "", ignoreCase = true)
            .replace("процессирует запрос", "", ignoreCase = true)
            .replace("{{char}}:", "", ignoreCase = true)
        content = content.split("result")[1]
        content = content.replaceFirst(":", "")
        content = content.replaceFirst("\"", "")
        content = content.replace("\n", "")
        content = content.substring(0, content.length - 1)
        content = content.trim()
        val res = jacksonObjectMapper().readValue(content, ResultDto::class.java)
        return res
    }

    private fun callApi(rq: GptRq): GptRs? {
        return try {
            gptRestClient.post()
                .body(rq)
                .retrieve()
                .toEntity(GptRs::class.java)
                .body
        } catch (e: Exception) {
            logger.error("Error while calling gpt api", e)
            null
        }
    }
}

data class GptRq (
    val model: String = "gpt-3.5-turbo",
    val messages: List<GptMessage>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GptRs (
    val choices: List<Choice>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Choice (
    val message: GptMessage? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GptMessage (
    val content: String? = null,
    val role: String? = "user",
)

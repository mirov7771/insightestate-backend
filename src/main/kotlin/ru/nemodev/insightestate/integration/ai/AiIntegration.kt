package ru.nemodev.insightestate.integration.ai

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import ru.nemodev.insightestate.integration.ai.dto.PromptRq
import ru.nemodev.insightestate.integration.ai.dto.PromptRs
import ru.nemodev.insightestate.integration.ai.dto.ResultDto
import ru.nemodev.insightestate.integration.ai.dto.ResultRs
import ru.nemodev.platform.core.logging.sl4j.Loggable

interface AiIntegration {
    fun generate(rq: String): ResultDto?
}

@Component
class AiIntegrationImpl (
    private val aiRestClient: RestClient
) : AiIntegration {

    companion object: Loggable {
        private const val PROPMT = "Убери лишнее из текста ниже и оставь только параметры объявления в формате JSON из текста, указанного в “Текст” JSON должен быть вложен в параметр 'result'.Если указывается: Тип - вилла, то “type“= “VILLA“, апартаменты, то “type“= “APARTMENT“, иначе “type“= NULL. Количество спален - параметр “rooms“. Зал - “gym“ = true, иначе - false. Магазин или магазины - “shop“ = true, иначе - false. Детская комната - “childRoom“ = true, иначе - false. Коворкинг - “coworking“  = true, иначе - false. Пляж - “beach“  = true, иначе - false. Паркинг или парковка - “parking“  = true, иначе - false. Пляж -  “beachName“ - одно из подходящих значений: Kata,Mai Khao,Layan,Bang Tao,Rawai,Kamala,Naithon,Karon,Surin,Nai Yang,Ao Yon, иначе - NULL. Время до пляжа на машине - “beachTravelTimesCar“. Время до пляжа пешком - “beachTravelTimesWalk“, если не понятно как добраться до пляжа - “beachTravelTimes“. Район -“district“ - одно из подходящих значений: Phuket Town, Kathu,Thalang, иначе - NULL. Время до аэропорта - “airportTravelTimes“. Цена от - “priceFrom“. Цена до - “priceTo“. “Цена от“ и “Цена до“ необходимо указывать валюту, параметр “currency“, если в тексте будет указано “рублей“, то “currency“ =“RUB“, если в тексте “бат“, то “currency“ = “THB“, иначе “currency“=“USD“. Дата окончания строительства - “buildEndYears“ в формате “YYYY“. Типы данных и Схема JSON-ответа: “{ “type“: “string“, “rooms“: “number“, “beach“: “bool“, “gym“:“bool“,“shop“:“bool“,“childRoom“:“bool“,parking:“bool“,“beachTravelTimesCar“:“number“,“beachTravelTimesWalk“:“number“,“beachTravelTimes“:“number“,“airportTravelTimes“:“number“,“priceFrom“:“number“,“priceTo“:“number“,“currency“:“string“,“buildEndYears“:“number“}“.  Типы данных для параметров - булевый: gym, shop, childRoom, beach, parking, числовой: beachTravelTimesCar, beachTravelTimesWalk, beachTravelTimes, priceFrom, priceTo, rooms. Текст:“%s“"
    }

    override fun generate(rq: String): ResultDto? {
        return prepareDto(
            callAi(rq)
        )
    }

    private fun callAi(rq: String): String? {
        return try {
            aiRestClient.post()
                .body(PromptRq(
                    prompt = PROPMT.format(rq)
                ))
                .retrieve()
                .toEntity(String::class.java)
                .body
        } catch (e: Exception) {
            logger.error("Error while generating $rq", e)
            null
        }
    }

    private fun prepareDto(aiResponse: String?): ResultDto? {
        var response = aiResponse
        try {
            if (response != null) {
                response = response.replace("}", "},")
                response = "[${response.trim()}]".replace(",]", "]")
                val rs = jacksonObjectMapper().readValue(response, object : TypeReference<List<PromptRs>>() {})
                val stringBuilder = StringBuilder()
                rs.forEach { stringBuilder.append(it.response) }
                val result = stringBuilder.toString()
                    .replace("```", "")
                    .replace("json", "")
                    .replace("},", "}")
                    .trim()
                val resultDto = jacksonObjectMapper().readValue(result, ResultRs::class.java)
                println("Response: $resultDto")
                return resultDto?.result
            }
            return null
        } catch (e: Exception) {
            logger.error("Error while generating $e")
            return null
        }
    }
}

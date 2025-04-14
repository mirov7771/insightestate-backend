package ru.nemodev.insightestate.integration.ai

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import ru.nemodev.insightestate.integration.ai.dto.*
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
        val rs = innerAi(rq)
        if (rs.isEmpty()) {
            return prepareDto(
                callAi(rq)
            )
        }
        return rs
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

    private fun innerAi(rq: String): ResultDto {
        //Тип проекта
        var type: String? = null
        if (rq.contains("Вилла", ignoreCase = true))
            type = "VILLA"
        if (rq.contains("Квартира", ignoreCase = true))
            type = "APARTMENT"

        //Город
        var city: String? = null
        if (rq.contains("Пхукет", ignoreCase = true))
            city = "Phuket"
        if (rq.contains("Бангкок", ignoreCase = true))
            city = "Bangkok"

        //Пляж
        var beach: String? = null
        if (rq.contains("Ката", ignoreCase = true))
            beach = "Kata"
        if (rq.contains("Катта", ignoreCase = true))
            beach = "Kata"
        if (rq.contains(" Као ", ignoreCase = true))
            beach = "Mai Khao"
        if (rq.contains("Лаян", ignoreCase = true))
            beach = "Layan"
        if (rq.contains("Лайан", ignoreCase = true))
            beach = "Layan"
        if (rq.contains("Лайян", ignoreCase = true))
            beach = "Layan"
        if (rq.contains("Патонг", ignoreCase = true))
            beach = "Patong"
        if (rq.contains("Раваи", ignoreCase = true))
            beach = "Rawai"
        if (rq.contains("Равай", ignoreCase = true))
            beach = "Rawai"
        if (rq.contains("Най харн", ignoreCase = true))
            beach = "Nai Harn"
        if (rq.contains("Найхарн", ignoreCase = true))
            beach = "Nai Harn"
        if (rq.contains("Камала", ignoreCase = true))
            beach = "Kamala"
        if (rq.contains("Карон", ignoreCase = true))
            beach = "Karon"
        if (rq.contains("Сурин", ignoreCase = true))
            beach = "Surin"
        if (rq.contains("Ной", ignoreCase = true))
            beach = "Kata Noi"
        if (rq.contains("Най янг", ignoreCase = true))
            beach = "Nai Yang"
        if (rq.contains("Найянг", ignoreCase = true))
            beach = "Nai Yang"
        if (rq.contains("Найтон", ignoreCase = true))
            beach = "Naithon"
        if (rq.contains("Найтхон", ignoreCase = true))
            beach = "Naithon"
        if (rq.contains("Банг Тао", ignoreCase = true))
            beach = "Bang Tao"
        if (rq.contains("БангТао", ignoreCase = true))
            beach = "Bang Tao"
        if (rq.contains("БангхТао", ignoreCase = true))
            beach = "Bang Tao"

        //Валюта
        var currency: String? = "THB"
        if (rq.contains(" руб", ignoreCase = true))
            currency = "RUB"
        if (rq.contains(" RUB", ignoreCase = true))
            currency = "RUB"
        if (rq.contains(" доллар", ignoreCase = true))
            currency = "USD"
        if (rq.contains(" USD", ignoreCase = true))
            currency = "USD"

        //Время до пляжа пешком
        var beachTravelTimesWalk: String? = null
        if (rq.contains(" минутах от пляжа", ignoreCase = true)) {
            beachTravelTimesWalk = getNumberFromString(rq, " минутах от пляжа")
        } else if (rq.contains(" минутах пешком от пляжа")) {
            beachTravelTimesWalk = getNumberFromString(rq, " минутах пешком от пляжа")
        } else if (rq.contains(" минут до пляжа")) {
            beachTravelTimesWalk = getNumberFromString(rq, " минут до пляжа")
        } else if (rq.contains(" минутах до пляжа")) {
            beachTravelTimesWalk = getNumberFromString(rq, " минутах до пляжа")
        } else if (rq.contains(" минутах пешком до пляжа")) {
            beachTravelTimesWalk = getNumberFromString(rq, " минутах пешком до пляжа")
        }

        //Спортзал
        var gym: String? = null
        if (rq.contains("спортзал", ignoreCase = true))
            gym = "true"

        //Парковка
        var parking: String? = null
        if (rq.contains("парковка", ignoreCase = true))
            parking = "true"

        //Цена от
        val priceFrom = getPriceFrom(rq)

        //Цена до
        val priceTo = getPriceTo(rq)

        return ResultDto(
            type = type,
            city = city,
            beach = beach,
            currency = currency,
            beachTravelTimesWalk = beachTravelTimesWalk,
            gym = gym,
            parking = parking,
            priceFrom = priceFrom,
            priceTo = priceTo,
        )
    }

    private fun getNumberFromString(value: String, split: String): String {
        val search = value.split(split)
        val spaces = search[0].split(" ")
        return spaces[spaces.size - 1].trim()
    }

    private fun getPriceFrom(value: String): String? {
        val searchString = value.replace("от пляжа", "")
            .replace("от аэропорта", "")
            .replace("от ТЦ", "")
            .replace("от торгового центра", "")
            .replace("от магазина", "")
        val split = searchString.split(" от ")
        if (split.isEmpty())
            return null
        if (split.size < 2)
            return null
        val spaces = split[1].split(" ")
        val price = spaces[0].trim()
        if (isNumeric(price)) {
            return if (value.contains("миллион"))
                "${price}000000"
            else
                "${price}000"
        }
        return null
    }

    private fun getPriceTo(value: String): String? {
        val searchString = value.replace("до пляжа", "")
            .replace("до аэропорта", "")
            .replace("до ТЦ", "")
            .replace("до торгового центра", "")
            .replace("до магазина", "")
        val split = searchString.split(" до ")
        if (split.isEmpty())
            return null
        if (split.size < 2)
            return null
        val spaces = split[1].split(" ")
        val price = spaces[0].trim()
        if (isNumeric(price)) {
            return if (value.contains("миллион"))
                "${price}000000"
            else if (value.contains(" млн "))
                "${price}000000"
            else
                "${price}000"
        }
        return null
    }

    fun isNumeric(toCheck: String): Boolean = toCheck.toDoubleOrNull() != null
}

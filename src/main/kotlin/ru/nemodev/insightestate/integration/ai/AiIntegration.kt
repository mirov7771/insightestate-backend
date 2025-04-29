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
        else if (rq.contains("Квартира", ignoreCase = true))
            type = "APARTMENT"
        else if (rq.contains("Виллу", ignoreCase = true))
            type = "VILLA"
        else if (rq.contains("Квартиру", ignoreCase = true))
            type = "APARTMENT"
        else if (rq.contains("Вилл", ignoreCase = true))
            type = "VILLA"
        else if (rq.contains("Квартир", ignoreCase = true))
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
        if (rq.contains("Kata", ignoreCase = true))
            beach = "Kata"
        if (rq.contains(" Као ", ignoreCase = true))
            beach = "Mai Khao"
        if (rq.contains(" Khao", ignoreCase = true))
            beach = "Mai Khao"
        if (rq.contains("Лаян", ignoreCase = true))
            beach = "Layan"
        if (rq.contains("Лайан", ignoreCase = true))
            beach = "Layan"
        if (rq.contains("Лайян", ignoreCase = true))
            beach = "Layan"
        if (rq.contains("Layan", ignoreCase = true))
            beach = "Layan"
        if (rq.contains("Патонг", ignoreCase = true))
            beach = "Patong"
        if (rq.contains("Paton", ignoreCase = true))
            beach = "Patong"
        if (rq.contains("Раваи", ignoreCase = true))
            beach = "Rawai"
        if (rq.contains("Равай", ignoreCase = true))
            beach = "Rawai"
        if (rq.contains("Рава", ignoreCase = true))
            beach = "Rawai"
        if (rq.contains("Rawa", ignoreCase = true))
            beach = "Rawai"
        if (rq.contains("Най харн", ignoreCase = true))
            beach = "Nai Harn"
        if (rq.contains("Найхарн", ignoreCase = true))
            beach = "Nai Harn"
        if (rq.contains("Най-харн", ignoreCase = true))
            beach = "Nai Harn"
        if (rq.contains("Nai Harn", ignoreCase = true))
            beach = "Nai Harn"
        if (rq.contains("NaiHarn", ignoreCase = true))
            beach = "Nai Harn"
        if (rq.contains("Nai-Harn", ignoreCase = true))
            beach = "Nai Harn"
        if (rq.contains("Камала", ignoreCase = true))
            beach = "Kamala"
        if (rq.contains("Kamala", ignoreCase = true))
            beach = "Kamala"
        if (rq.contains("Карон", ignoreCase = true))
            beach = "Karon"
        if (rq.contains("Karon", ignoreCase = true))
            beach = "Karon"
        if (rq.contains("Сурин", ignoreCase = true))
            beach = "Surin"
        if (rq.contains("Surin", ignoreCase = true))
            beach = "Surin"
        if (rq.contains("Ной", ignoreCase = true))
            beach = "Kata Noi"
        if (rq.contains("Noi", ignoreCase = true))
            beach = "Kata Noi"
        if (rq.contains("Най янг", ignoreCase = true))
            beach = "Nai Yang"
        if (rq.contains("Най-янг", ignoreCase = true))
            beach = "Nai Yang"
        if (rq.contains("Найянг", ignoreCase = true))
            beach = "Nai Yang"
        if (rq.contains("Nai Yang", ignoreCase = true))
            beach = "Nai Yang"
        if (rq.contains("Nai-Yang", ignoreCase = true))
            beach = "Nai Yang"
        if (rq.contains("NaiYang", ignoreCase = true))
            beach = "Nai Yang"
        if (rq.contains("Найтон", ignoreCase = true))
            beach = "Naithon"
        if (rq.contains("Найтхон", ignoreCase = true))
            beach = "Naithon"
        if (rq.contains("Naithon", ignoreCase = true))
            beach = "Naithon"
        if (rq.contains("Банг Тао", ignoreCase = true))
            beach = "Bang Tao"
        if (rq.contains("БангТао", ignoreCase = true))
            beach = "Bang Tao"
        if (rq.contains("БангхТао", ignoreCase = true))
            beach = "Bang Tao"
        if (rq.contains("Банг-Тао", ignoreCase = true))
            beach = "Bang Tao"
        if (rq.contains("Bang Tao", ignoreCase = true))
            beach = "Bang Tao"
        if (rq.contains("Bang-Tao", ignoreCase = true))
            beach = "Bang Tao"

        //Валюта
        var currency: String? = "USD"
        if (rq.contains(" руб", ignoreCase = true))
            currency = "RUB"
        if (rq.contains(" RUB", ignoreCase = true))
            currency = "RUB"
        if (rq.contains(" доллар", ignoreCase = true))
            currency = "USD"
        if (rq.contains(" USD", ignoreCase = true))
            currency = "USD"
        if (rq.contains(" батт", ignoreCase = true))
            currency = "THB"

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
        } else if (rq.contains(" минутах ")) {
            beachTravelTimesWalk = getNumberFromString(rq, " минутах ")
        } else if (rq.contains(" минуте ")) {
            beachTravelTimesWalk = getNumberFromString(rq, " минуте ")
        }

        //Время до аэропорта
        var airportTravelTimes: String? = null
        if (rq.contains(" минутах от аэропорта", ignoreCase = true)) {
            airportTravelTimes = getNumberFromString(rq, " минутах от аэропорта")
        } else if (rq.contains(" минутах езды от аэропорта")) {
            airportTravelTimes = getNumberFromString(rq, " минутах езды от аэропорта")
        } else if (rq.contains(" минут до аэропорта")) {
            airportTravelTimes = getNumberFromString(rq, " минут до аэропорта")
        } else if (rq.contains(" минутах до аэропорта")) {
            airportTravelTimes = getNumberFromString(rq, " минутах до аэропорта")
        } else if (rq.contains(" минутах езды до аэропорта")) {
            airportTravelTimes = getNumberFromString(rq, " минутах езды до аэропорта")
        } else if (rq.contains(" минут езды ")) {
            airportTravelTimes = getNumberFromString(rq, " минут езды ")
        }

        //Время до ТЦ
        var mallTravelTimes: String? = null
        if (rq.contains(" минутах от ТЦ", ignoreCase = true)) {
            mallTravelTimes = getNumberFromString(rq, " минутах от ТЦ")
        } else if (rq.contains(" минутах пешком от ТЦ")) {
            mallTravelTimes = getNumberFromString(rq, " минутах пешком от ТЦ")
        } else if (rq.contains(" минут до ТЦ")) {
            mallTravelTimes = getNumberFromString(rq, " минут до ТЦ")
        } else if (rq.contains(" минутах до ТЦ")) {
            mallTravelTimes = getNumberFromString(rq, " минутах до ТЦ")
        } else if (rq.contains(" минутах пешком до ТЦ")) {
            mallTravelTimes = getNumberFromString(rq, " минутах пешком до ТЦ")
        }

        //Спортзал
        var gym: String? = null
        if (rq.contains("спортзал", ignoreCase = true))
            gym = "true"
        else if (rq.contains(" зал", ignoreCase = true))
            gym = "true"
        else if (rq.contains(" тренажер", ignoreCase = true))
            gym = "true"

        //Парковка
        var parking: String? = null
        if (rq.contains("парковка", ignoreCase = true))
            parking = "true"

        //Цена от
        var priceFrom = getPriceFrom(rq)

        //Цена до
        var priceTo = getPriceTo(rq)

        //Кол-во комнат
        val searchRooms = rq.replace("одной", "1")
            .replace("одна", "1")
            .replace("двумя", "2")
            .replace("две", "2")
            .replace("тремя", "3")
            .replace("три", "3")
            .replace("четырьмя", "4")
            .replace("четыре", "4")
        val rooms = if (rq.contains("студи", ignoreCase = true))
            "0"
        else if (rq.contains("1 комнат", ignoreCase = true))
            "1"
        else if (rq.contains("2 комнат", ignoreCase = true))
            "2"
        else if (rq.contains("3 комнат", ignoreCase = true))
            "3"
        else if (rq.contains("4 комнат", ignoreCase = true))
            "4"
        else if (rq.contains("1 спал", ignoreCase = true))
            "1"
        else if (rq.contains("2 спал", ignoreCase = true))
            "2"
        else if (rq.contains("3 спал", ignoreCase = true))
            "3"
        else if (rq.contains("4 спал", ignoreCase = true))
            "4"
        else
            null

        //Дата окончания строительства
        val buildEndYears = if (rq.contains("2025"))
            "2025"
        else if (rq.contains("2026"))
            "2026"
        else if (rq.contains("2027"))
            "2027"
        else if (rq.contains("2028"))
            "2028"
        else if (rq.contains("2029"))
            "2029"
        else if (rq.contains("2030"))
            "2030"
        else
            null

        //Наличие УК
        var isUk: String? = null
        if (rq.contains("с наличием УК", ignoreCase = true) ||
            rq.contains("с УК", ignoreCase = true) ||
            rq.contains("с управляющей компанией", ignoreCase = true) ||
            rq.contains("с наличием управляющей компанией", ignoreCase = true)) {
            isUk = "true"
        } else if (rq.contains("без наличия УК", ignoreCase = true) ||
            rq.contains("без УК", ignoreCase = true) ||
            rq.contains("без управляющей компанией", ignoreCase = true) ||
            rq.contains("без наличия управляющей компанией", ignoreCase = true)) {
            isUk = "false"
        }

        //Общая оценка
        var rating: String? = null
        if (rq.contains("9 бал", ignoreCase = true))
            rating = "9"
        else if (rq.contains("8 бал", ignoreCase = true))
            rating = "8"
        else if (rq.contains("7 бал", ignoreCase = true))
            rating = "7"

        //ROI
        val roi = if (rq.contains("ROI", ignoreCase = true)) "true" else null

        val childRoom = if (rq.contains("детск", ignoreCase = true))
            "true"
        else if (rq.contains("детей", ignoreCase = true))
            "true"
        else
            null

        if (airportTravelTimes != null) {
            if ("${airportTravelTimes}000" == priceFrom)
                priceFrom = null
            if ("${airportTravelTimes}000" == priceTo)
                priceTo = null
        }

        if (beachTravelTimesWalk != null) {
            if ("${beachTravelTimesWalk}000" == priceFrom)
                priceFrom = null
            if ("${beachTravelTimesWalk}000" == priceTo)
                priceTo = null
        }

        if (mallTravelTimes != null) {
            if ("${mallTravelTimes}000" == priceFrom)
                priceFrom = null
            if ("${mallTravelTimes}000" == priceTo)
                priceTo = null
        }

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
            rooms = rooms,
            buildEndYears = buildEndYears,
            airportTravelTimes = airportTravelTimes,
            isUk = isUk,
            rating = rating,
            roi = roi,
            mallTravelTimes = mallTravelTimes,
            childRoom = childRoom
        )
    }

    private fun getNumberFromString(value: String, split: String): String {
        val search = value.split(split)
        val spaces = search[0].split(" ")
        return spaces[spaces.size - 1].trim()
    }

    private fun getPriceFrom(value: String): String? {
        var searchString = value.replace("от пляжа", "")
            .replace("от аэропорта", "")
            .replace("от ТЦ", "")
            .replace("от торгового центра", "")
            .replace("от магазина", "")
            .replace("до моря", "")
            .replace(".", "")

        searchString = searchString.replace(" ста ", " 100 ")
            .replace(" сто ", " 100 ")
            .replace("двухсот", "200")
            .replace("двести", "200")
            .replace("трехсот", "300")
            .replace("трехста", "300")
            .replace("триста", "300")
            .replace("четыреста", "400")
            .replace("четырехсот", "400")
            .replace("пятисот", "500")
            .replace("пятиста", "500")
            .replace("шестисот", "600")
            .replace("шестиста", "600")
            .replace("семисот", "700")
            .replace("семьсот", "700")
            .replace("семиста", "700")
            .replace("восьмисот", "800")
            .replace("восемьсот", "800")
            .replace("восьмиста", "800")
            .replace("девятьсот", "900")
            .replace("девятисот", "900")
            .replace("девятиста", "900")
            .replace("одного", "1")
            .replace("двух", "2")
            .replace("трех", "3")
            .replace("четырех", "4")
            .replace("пяти", "5")
            .replace("шести", "6")
            .replace("семи", "7")
            .replace("восьми", "8")
            .replace("девяти", "9")
            .replace("0к", "0")
            .replace("0k", "0")

        var split = searchString.split(" от ", ignoreCase = true)

        if (split.isEmpty() || split.size < 2) {
            searchString = searchString.replace("бюджет", "")
            split = searchString.split(" минимальный ")
            if (split.isEmpty() || split.size < 2)
                return null
        }
        val spaces = split[1].trim().split(" ")
        val price = spaces[0].trim()
        if (isNumeric(price)) {
            return if (value.contains("миллион"))
                "${price}000000"
            else if (value.contains(" млн "))
                "${price}000000"
            else if (value.contains(" млн."))
                "${price}000000"
            else if (price.length > 3)
                price
            else
                "${price}000"
        }
        return null
    }

    private fun getPriceTo(value: String): String? {
        var searchString = value.replace("до пляжа", "")
            .replace("до аэропорта", "")
            .replace("до ТЦ", "")
            .replace("до торгового центра", "")
            .replace("до магазина", "")
            .replace("до моря", "")
            .replace(".", "")
        searchString = searchString.replace(" ста ", " 100 ")
            .replace(" сто ", " 100 ")
            .replace("двухсот", "200")
            .replace("двести", "200")
            .replace("трехсот", "300")
            .replace("трехста", "300")
            .replace("триста", "300")
            .replace("четыреста", "400")
            .replace("четырехсот", "400")
            .replace("пятисот", "500")
            .replace("пятиста", "500")
            .replace("шестисот", "600")
            .replace("шестиста", "600")
            .replace("семисот", "700")
            .replace("семьсот", "700")
            .replace("семиста", "700")
            .replace("восьмисот", "800")
            .replace("восемьсот", "800")
            .replace("восьмиста", "800")
            .replace("девятьсот", "900")
            .replace("девятисот", "900")
            .replace("девятиста", "900")
            .replace("одного", "1")
            .replace("двух", "2")
            .replace("трех", "3")
            .replace("четырех", "4")
            .replace("пяти", "5")
            .replace("шести", "6")
            .replace("семи", "7")
            .replace("восьми", "8")
            .replace("девяти", "9")
            .replace("0к", "0")
            .replace("0k", "0")
        var split = searchString.split(" до ", ignoreCase = true)
        if (split.isEmpty() || split.size < 2) {
            searchString = searchString.replace("бюджет", "")
            split = searchString.split(" максимальный ")
            if (split.isEmpty() || split.size < 2)
                return null
        }
        val spaces = split[1].trim().split(" ")
        val price = spaces[0].trim()
        if (isNumeric(price)) {
            return if (value.contains("миллион"))
                "${price}000000"
            else if (value.contains(" млн "))
                "${price}000000"
            else if (value.contains(" млн."))
                "${price}000000"
            else if (price.length > 3)
                price
            else
                "${price}000"
        }
        return null
    }

    fun isNumeric(toCheck: String): Boolean = toCheck.toDoubleOrNull() != null
}

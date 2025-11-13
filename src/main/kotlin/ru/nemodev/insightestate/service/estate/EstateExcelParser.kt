package ru.nemodev.insightestate.service.estate

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import ru.nemodev.insightestate.entity.*
import ru.nemodev.insightestate.extension.*
import ru.nemodev.platform.core.extensions.isNotNullOrEmpty
import ru.nemodev.platform.core.extensions.nullIfEmpty
import ru.nemodev.platform.core.extensions.scaleAndRoundAmount
import ru.nemodev.platform.core.logging.sl4j.Loggable
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

interface EstateExcelParser {
    fun parse(inputStream: InputStream): LoadDto
}

@Component
class EstateExcelParserImpl : EstateExcelParser {

    companion object : Loggable {
        private val buildEndDateParseFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private val buildEndDateSaveFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    override fun parse(inputStream: InputStream): LoadDto {
        val workbook = XSSFWorkbook(inputStream)
        val estateSheet = workbook.getSheet("База")
        val estates = estateSheet
            .mapIndexedNotNull { index, row ->
                if (row.getString("F") != "Done") {
                    return@mapIndexedNotNull null
                }
                try {
                    parseRow(row)
                } catch (e: Exception) {
                    logError(e) { "Ошибка парсинга объекта id = ${row.getString("B")} строка = ${index + 1}" }
                    null
                }
            }

        val unitSheet = workbook.getSheet("Прайс платформа")
        val units = unitSheet.mapIndexedNotNull { index, row ->
            if (index == 0)
                return@mapIndexedNotNull null
            if (row.getString("A").isNullOrEmpty())
                return@mapIndexedNotNull null
            parseUnitRow(row, index)
        }
        return LoadDto(
            estates = estates,
            units = units
        )
    }

    private fun parseUnitRow(
        row: Row,
        index: Int,
    ): UnitEntity? {
        fun parseNumber(value: String?): String? {
            if (value.isNullOrEmpty())
                return null
            return try {
                BigDecimal(value.replace(",", ".").replace(" ", "")).setScale(0, RoundingMode.HALF_UP).toString()
            } catch (_: Exception) {
                value
            }
        }
        try {
            var image = row.getString("L") ?: ""
            if (image.isNotBlank()) {
                image = "https://lotsof.properties/estate-images/$image.webp"
            }
            return UnitEntity(
                id = UUID.randomUUID(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                code = row.getString("A")!!,
                corpus = row.getString("C") ?: "",
                number = row.getString("D") ?: "",
                floor = row.getString("E") ?: "",
                rooms = row.getString("F"),
                square = parseNumber(row.getString("G")),
                priceSq = parseNumber(row.getString("H")),
                price = parseNumber(row.getString("I")),
                planImage = image
            ).apply { isNew = true }
        } catch (e: Exception) {
            logError(e) { "Ошибка парсинга юнита с кодом = ${row.getString("A")} строка = ${index + 1}" }
            return null
        }
    }

    private fun parseRow(row: Row): EstateEntity {
        return EstateEntity(
            estateDetail = EstateDetail(
                lat = row.getString("JB"),
                lon = row.getString("JC"),
                toolTip1 = row.getString("IV"),
                toolTip2 = row.getString("IW"),
                toolTip3 = row.getString("IX"),
                projectId = row.getString("B")!!,
                name = row.getString("C")!!,
                shortDescriptionRu = row.getString("IS"),
                shortDescriptionEn = row.getString("IT"),

                landPurchased = row.getBoolean("D"),
                eiaEnabled = row.getBoolean("E"),
                developer = EstateDeveloper(
                    name = row.getString("H")!!,
                    country = row.getString("AJ"),
                    //yearOfFoundation = row.getInt("AL"),
                    phone = row.getString("AL"),
                    email = row.getString("AM")
                ),
                grade = EstateGrade(
                    main = row.getBigDecimal("I", 1)!!,
                    investmentSecurity = row.getBigDecimal("J", 1)!!,
                    investmentPotential = row.getBigDecimal("O", 1)!!,
                    projectLocation = row.getBigDecimal("T", 1)!!,
                    comfortOfLife = row.getBigDecimal("Y", 1)!!,
                ),
                projectCount = ProjectCount(
                    total = row.getInt("AG")!!,
                    build = row.getInt("AH")!!,
                    finished = row.getInt("AI")!!,
                    deviationFromDeadline = row.getInt("AM"),
                ),
                status = when (row.getString("AN") ?: "") {
                    "Строится" -> EstateStatus.BUILD
                    "Сдан" -> EstateStatus.FINISHED
                    else -> EstateStatus.UNKNOWN
                },
                saleStartDate = null, // TODO столбец AO вроде поле нигде не требуется в таблицу нужно поменять формат на дату
                buildEndDate = row.getLocalDate("AP")
                    ?: row.getString("AP")?.nullIfEmpty()?.let { LocalDate.parse(it, buildEndDateParseFormatter) },
                unitCount = UnitCount(
                    total = row.getInt("AQ") ?: 0,
                    sailed = row.getInt("AR"),
                    available = row.getInt("AS"),
                ),
                type = row.getString("EA").let {
                    if (it == null) EstateType.APARTMENT else EstateType.VILLA
                },
                level = when (row.getString("BE") ?: "") {
                    "Премиум" -> EstateLevelType.PREMIUM
                    "Люкс" -> EstateLevelType.LUX
                    "Комфорт" -> EstateLevelType.COMFORT
                    else -> EstateLevelType.UNKNOWN
                },
                product = when (row.getString("BG") ?: "") {
                    "Инвест" -> EstateProductType.INVESTMENT
                    "Резиденция" -> EstateProductType.RESIDENCE
                    else -> EstateProductType.UNKNOWN
                },
                profitability = EstateProfitability(
                    roi = row.getBigDecimalFromPercent("HN", 1) ?: BigDecimal.ZERO,
                    roiSummary = row.getBigDecimalFromPercent("IR", 0) ?: BigDecimal.ZERO,
                    irr = row.getBigDecimalFromPercent("HM", 1) ?: BigDecimal.ZERO,
                    capRateFirstYear = row.getBigDecimalFromPercent("HL", 1) ?: BigDecimal.ZERO,
                ),
                location = EstateLocation(
                    name = row.getString("AU")!!,
                    district = row.getString("AV")!!,
                    beach = row.getString("AW")!!,
                    mapUrl = row.getString("IQ")!!,
                    city = row.getString("AK")!!
                ),
                infrastructure = EstateInfrastructure(
                    beachTime = TravelTime(
                        walk = row.getInt("AX")!!,
                        car = row.getInt("AY")!!,
                    ),
                    airportTime = TravelTime(
                        walk = null,
                        car = row.getInt("AZ")!!,
                    ),
                    mallTime = TravelTime(
                        walk = row.getInt("BB")!!,
                        car = row.getInt("BA")!!,
                    ),
                    school = EstateInfrastructure.School(
                        radius = row.getBigDecimal("BC", 1) ?: BigDecimal.ZERO,
                        name = row.getString("BD")
                    ),
                ),
                options = EstateOptions(
                    parkingSize = row.getInt("BH")?.let { if (it == 0) null else it },
                    gym = row.getBoolean("CF"),
                    childRoom = row.getBoolean("CG"),
                    shop = row.getBoolean("CH"),
                    entertainment = row.getBoolean("CI"),
                    coworking = row.getBoolean("CJ"), // TODO Работа это коворкинг?
                    petFriendly = row.getBoolean("CL")
                ),
                managementCompany = ManagementCompany(
                    enabled =  row.getBoolean("CB"),
                ),
                price = MinMaxAvgParam(
                    min = getMinPrice(row, listOf("DI", "DL", "DO", "DR", "DU", "DX", "II", "IK", "IM", "IO", "EA")),
                    max = getMaxPrice(row, listOf("DK", "DN", "DQ", "DT", "DW", "DZ", "IJ", "IL", "IN", "IP", "EC")),
                    avg = row.getBigDecimal("EB", 0) // TODO средняя стоимость указана только для вилл
                ),
                ceilingHeight = row.getBigDecimal("BJ", 1),
                floors = row.getInt("BK"),
                roomLayouts = RoomLayouts(
                    studio = getRoomParams(row, listOf("CM", "CO", "CN"), listOf("DI", "DK", "DJ"), listOf("HO", "HP")),
                    one = getRoomParams(row, listOf("CP", "CR", "CQ"), listOf("DL", "DN", "DM"), listOf("HQ", "HR")),
                    two = getRoomParams(row, listOf("CS", "CU", "CT"), listOf("DO", "DQ", "DP"), listOf("HS", "HT")),
                    three = getRoomParams(row, listOf("CV", "CX", "CW"), listOf("DR", "DT", "DS"), listOf("HU", "HV")),
                    four = getRoomParams(row, listOf("CY", "DA", "CZ"), listOf("DU", "DW", "DV"), listOf("HW", "HX")),
                    five = getRoomParams(row, listOf("DB", "DD", "DC"), listOf("DX", "DZ", "DY"), listOf("HY", "HZ")),
                    villaTwo = getRoomParams(row, listOf("DF", "DH", "DG"), listOf("II", "IJ", "EB"), listOf("IA", "IB")),
                    villaThree = getRoomParams(row, listOf("DF", "DH", "DG"), listOf("IK", "IL", "EB"), listOf("IC", "ID")),
                    villaFour = getRoomParams(row, listOf("DF", "DH", "DG"), listOf("IM", "IN", "EB"), listOf("IE", "IF")),
                    villaFive = getRoomParams(row, listOf("DF", "DH", "DG"), listOf("IO", "IP", "EB"), listOf("IG", "IH")),
                ),
                paymentPlan = row.getString("EO")
            )
        )
    }

    private fun getMinPrice(row: Row, cellNames: List<String>): BigDecimal {
        var min = BigDecimal.valueOf(1000000000)
        cellNames.forEach {
            val curMin = row.getBigDecimal(it)
            if (curMin != null && curMin > BigDecimal.ZERO && curMin > BigDecimal("0.0") && curMin < min) {
                min = curMin
            }
        }

        return min.scaleAndRoundAmount()
    }

    private fun getMaxPrice(row: Row, cellNames: List<String>): BigDecimal {
        var max = BigDecimal.valueOf(-1)
        cellNames.forEach {
            val curMax = row.getBigDecimal(it)
            if (curMax != null && curMax > max) {
                max = curMax
            }
        }

        return max.scaleAndRoundAmount()
    }

    private fun getRoomParams(
        row: Row,
        pricePerMeterCellNames: List<String>, // min max avg
        priceCellNames: List<String>,         // min max avg
        squareCellNames: List<String>,        // min max
    ): RoomParams? {
        val pricePerMeter = getMinMaxAvgParam(row, pricePerMeterCellNames)
        val price = getMinMaxAvgParam(row, priceCellNames)
        val square = getMinMaxAvgParam(row, squareCellNames)

        if (price == null && square == null) {
            return null
        }

        return RoomParams(
            pricePerMeter = pricePerMeter,
            price = price,
            square = square,
        )
    }

    private fun getMinMaxAvgParam(row: Row, cellNames: List<String>): MinMaxAvgParam? {
        val min = row.getString(cellNames[0])?.replace(" ", "")?.nullIfEmpty()?.let {
            if (it == "0") null
            else it.getBigDecimal()
        }
        val max = row.getString(cellNames[1])?.replace(" ", "")?.nullIfEmpty()?.let {
            if (it == "0") null
            else it.getBigDecimal()
        }
        val avg = if (cellNames.size == 3)
            row.getString(cellNames[2])?.replace(" ", "")?.nullIfEmpty()?.let {
                if (it == "0") null else it.getBigDecimal()
            }
            else null

        if (min == null || max == null) {
            return null
        }
        return MinMaxAvgParam(
            min = min,
            max = max,
            avg  = avg
        )
    }

}

data class LoadDto (
    val estates: List<EstateEntity>? = null,
    val units: List<UnitEntity>? = null,
)

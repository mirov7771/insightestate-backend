package ru.nemodev.insightestate.service.estate

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import ru.nemodev.insightestate.entity.*
import ru.nemodev.platform.core.extensions.nullIfEmpty
import ru.nemodev.platform.core.extensions.scaleAndRoundAmount
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId

interface EstateExcelParser {
    fun parse(): List<EstateEntity>
}

// TODO вынести в extension
fun Row.getCellByName(name: String): XSSFCell? {
    val cell = getCell(CellReference.convertColStringToIndex(name)) ?: return null
    return cell as XSSFCell
}

fun Row.getString(cellName: String): String? {
    val cell = getCellByName(cellName) ?: return null
    val value = if (cell.cellType == CellType.STRING) cell.stringCellValue else cell.rawValue

    return value?.nullIfEmpty()
}

fun Row.getBigDecimal(cellName: String, scale: Int? = null): BigDecimal? {
    val value = this.getString(cellName)
        ?.replace(" ", "")
        ?.replace(",", ".")
        ?.replace("%", "")
        ?.nullIfEmpty()
        ?.toBigDecimal()

    if (value == null || scale == null) {
        return value
    }
    return value.scaleAndRoundAmount(scale, RoundingMode.HALF_UP)
}

fun Row.getBigDecimalFromPercent(cellName: String, scale: Int): BigDecimal? {
    val value = getBigDecimal(cellName)?.toDouble() ?: return null
    return (value * 100).toBigDecimal().scaleAndRoundAmount(scale)
}

fun Row.getInt(cellName: String): Int? {
    return this.getString(cellName)
        ?.filter { it.isDigit() }?.toInt()
}

fun Row.getBoolean(cellName: String): Boolean {
    val cell = getCellByName(cellName) ?: return false
    if (cell.cellType == CellType.BOOLEAN) {
        return cell.booleanCellValue
    }
    return this.getString(cellName)?.lowercase() == "да"
}

fun Row.getLocalDate(cellName: String): LocalDate? {
    return this.getCellByName(cellName)?.dateCellValue?.let {
        LocalDate.ofInstant(it.toInstant(), ZoneId.of("Europe/Moscow"))
    }
}


@Component
class EstateExcelParserImpl : EstateExcelParser {

    override fun parse(): List<EstateEntity> {
        val estateProjectExcelFile = ClassPathResource("data/estate.xlsx")
            .inputStream.use { it.readAllBytes() }

        val workbook = XSSFWorkbook(estateProjectExcelFile.inputStream())

        val estates = workbook.getSheetAt(0)
            .filter { it.getString("E") == "Done" }
            .map { row ->
                EstateEntity(
                    estateDetail = EstateDetail(
                        projectId = row.getString("A")!!,
                        name = row.getString("B")!!,
                        shortDescriptionRu = row.getString("IR"),
                        shortDescriptionEn = row.getString("IS"),

                        landPurchased = row.getBoolean("C"),
                        eiaEnabled = row.getBoolean("D"),
                        readyStatus = EstateReadyStatus.DONE,
                        priority = row.getInt("F"),
                        developer = EstateDeveloper(
                            name = row.getString("G")!!,
                            country = row.getString("AI"),
                            yearOfFoundation = row.getInt("AK"),
                        ),
                        grade = EstateGrade(
                            final = row.getBigDecimal("H", 1)!!,
                            investmentSecurity = row.getBigDecimal("I", 1)!!,
                            investmentPotential = row.getBigDecimal("N", 1)!!,
                            projectLocation = row.getBigDecimal("S", 1)!!,
                            comfortOfLife = row.getBigDecimal("X", 1)!!,
                        ),
                        projectCount = ProjectCount(
                            total = row.getInt("AF")!!,
                            build = row.getInt("AG")!!,
                            finished = row.getInt("AH")!!,
                            deviationFromDeadline = row.getInt("AL"),
                        ),
                        status = when (row.getString("AM")!!) {
                            "Строится" -> EstateStatus.BUILD
                            "Сдан" -> EstateStatus.FINISHED
                            else -> EstateStatus.UNKNOWN
                        },
                        saleDate = null, // TODO вроде поле нигде не требуется в таблицу нужно поменять формат на дату
                        buildEndDate = row.getLocalDate("AO"),
                        unitCount = UnitCount(
                            total = row.getInt("AP")!!,
                            sailed = row.getInt("AQ"),
                            available = row.getInt("AR")!!,
                        ),
                        constructionSchedule = row.getString("AS"),
                        type = row.getString("EA").let {
                            if (it == null) EstateType.APARTMENT else EstateType.VILLA
                        },
                        level = when (row.getString("BD")!!) {
                            "Премиум" -> EstateLevelType.PREMIUM
                            "Люкс" -> EstateLevelType.LUX
                            "Комфорт" -> EstateLevelType.COMFORT
                            else -> EstateLevelType.UNKNOWN
                        },
                        product = when (row.getString("BF")!!) {
                            "Инвест" -> EstateProductType.INVESTMENT
                            "Резиденция" -> EstateProductType.RESIDENCE
                            else -> EstateProductType.UNKNOWN
                        },
                        profitability = EstateProfitability(
                            roi = row.getBigDecimalFromPercent("HM", 1)!!,
                            roiSummary = row.getBigDecimalFromPercent("IQ", 0)!!,
                            irr = row.getBigDecimalFromPercent("HL", 1)!!,
                            capRateFirstYear = row.getBigDecimalFromPercent("HK", 1)!!,
                        ),
                        location = EstateLocation(
                            name = row.getString("AT")!!,
                            district = row.getString("AU")!!,
                            beach = row.getString("AV")!!,
                            mapUrl = row.getString("IP")!!
                        ),
                        infrastructure = EstateInfrastructure(
                            beachTime = TravelTime(
                                walk = row.getInt("AW")!!,
                                car = row.getInt("AX")!!,
                            ),
                            airportTime = TravelTime(
                                walk = null,
                                car = row.getInt("AY")!!,
                            ),
                            mallTime = TravelTime(
                                walk = row.getInt("BA")!!,
                                car = row.getInt("AZ")!!,
                            ),
                            schoolRadius = row.getBigDecimal("BB", 1)!!,
                            nurserySchoolRadius = row.getBigDecimal("BC", 1),
                        ),
                        options = EstateOptions(
                            parkingSize = row.getInt("BG"),
                            gym = row.getBoolean("CE"),
                            childRoom = row.getBoolean("CF"),
                            shop = row.getBoolean("CG"),
                            entertainment = row.getBoolean("CH"),
                            coWorking = false // TODO нет колонки?
                        ),
                        price = MinMaxAvgParam(
                            min = getMinPrice(row, listOf("DH", "DK", "DN", "DQ", "DT", "DW", "IH", "IJ", "IL", "IN")),
                            max = getMaxPrice(row, listOf("DH", "DK", "DN", "DQ", "DT", "DW", "IH", "IJ", "IL", "IN")),
                            avg = row.getBigDecimal("EA") // TODO средняя стоимость указана только для вилл
                        ),
                        profitAmount = null, // TODO откуда брать?
                        profitTerm = null,   // TODO откуда брать?
                        ceilingHeight = row.getBigDecimal("BI", 1),
                        floors = null, // TODO колонки с этажами нет?
                        roomLayouts = RoomLayouts(
                            studio = getRoomParams(row, listOf("CL", "CN", "CM"), listOf("DH", "DJ", "DI"), listOf("HN", "HO")),
                            one = getRoomParams(row, listOf("CO", "CQ", "CP"), listOf("DK", "DM", "DL"), listOf("HP", "HQ")),
                            two = getRoomParams(row, listOf("CR", "CT", "CS"), listOf("DN", "DP", "DO"), listOf("HR", "HS")),
                            three = getRoomParams(row, listOf("CU", "CW", "CV"), listOf("DQ", "DS", "DR"), listOf("HT", "HU")),
                            four = getRoomParams(row, listOf("CX", "CZ", "CY"), listOf("DT", "DV", "DU"), listOf("HV", "HW")),
                            five = getRoomParams(row, listOf("DA", "DC", "DB"), listOf("DW", "DY", "DX"), listOf("HX", "HY")),
                            villaTwo = getRoomParams(row, listOf("DE", "DG", "DF"), listOf("IH", "II", "EA"), listOf("HZ", "IA")),
                            villaThree = getRoomParams(row, listOf("DE", "DG", "DF"), listOf("IJ", "DK", "EA"), listOf("IB", "IC")),
                            villaFour = getRoomParams(row, listOf("DE", "DG", "DF"), listOf("IL", "IM", "EA"), listOf("ID", "IE")),
                            villaFive = getRoomParams(row, listOf("DE", "DG", "DF"), listOf("IN", "IO", "EA"), listOf("IF", "IG")),
                        ),
                        projectImage = null,
                        images = null
                    )
                )
            }

        return estates
    }

    private fun getMinPrice(row: Row, cellNames: List<String>): BigDecimal {
        var min = BigDecimal.valueOf(1000000000)
        cellNames.forEach {
            val curMin = row.getBigDecimal(it)
            if (curMin != null && curMin != BigDecimal.ZERO && curMin < min) {
                min = curMin
            }
        }

        return min.scaleAndRoundAmount(0)
    }

    private fun getMaxPrice(row: Row, cellNames: List<String>): BigDecimal {
        var max = BigDecimal.valueOf(-1)
        cellNames.forEach {
            val curMax = row.getBigDecimal(it)
            if (curMax != null && curMax > max) {
                max = curMax
            }
        }

        return max.scaleAndRoundAmount(0)
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

        if (pricePerMeter == null || price == null || square == null) {
            return null
        }

        return RoomParams(
            pricePerMeter = pricePerMeter,
            price = price,
            square = square,
        )
    }

    private fun getMinMaxAvgParam(row: Row, cellNames: List<String>): MinMaxAvgParam? {
        val min = row.getString(cellNames[0])?.replace(" ", "")?.replace("0", "")?.nullIfEmpty()?.toBigDecimal()?.scaleAndRoundAmount(0)
        val max = row.getString(cellNames[1])?.replace(" ", "")?.replace("0", "")?.nullIfEmpty()?.toBigDecimal()?.scaleAndRoundAmount(0)
        val avg = if (cellNames.size == 3)
            row.getString(cellNames[2])?.replace(" ", "")?.replace("0", "")?.nullIfEmpty()?.toBigDecimal()?.scaleAndRoundAmount(0)
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
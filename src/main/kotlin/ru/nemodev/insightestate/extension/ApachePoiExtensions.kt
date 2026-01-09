package ru.nemodev.insightestate.extension

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFCell
import ru.nemodev.platform.core.extensions.nullIfEmpty
import ru.nemodev.platform.core.extensions.scaleAndRoundAmount
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId

fun Row.getCellByName(name: String): XSSFCell? {
    val cell = getCell(CellReference.convertColStringToIndex(name)) ?: return null
    return cell as XSSFCell
}

fun Row.getString(cellName: String): String? {
    val cell = getCellByName(cellName) ?: return null
    val value = if (cell.cellType == CellType.STRING) cell.stringCellValue else cell.rawValue

    return value?.trim()?.nullIfEmpty()
}

fun Row.getBigDecimal(cellName: String, scale: Int? = null): BigDecimal? {
    try {
        val cell = getCellByName(cellName) ?: return null
        if (cell.cellType == CellType.NUMERIC) {
            if (getString(cellName).isNullOrEmpty()) {
                return null
            }

            val value = cell.numericCellValue.toBigDecimal()
            if (scale != null) {
                return value.scaleAndRoundAmount()
            }
            return value
        }

        val value = this.getString(cellName)
            ?.replace(" ", "")
            ?.replace(",", ".")
            ?.replace("%", "")
            ?.replace(" ", "")
            ?.nullIfEmpty()
            ?.toBigDecimal()

        if (value == null || scale == null) {
            return value
        }
        return value.scaleAndRoundAmount()
    } catch (_: Exception) {
        return BigDecimal.ZERO
    }
}

fun String.getBigDecimal(): BigDecimal {
    val value = this.replace(" ", "")
        .replace(",", ".")
        .replace("%", "")
        .replace(" ", "")
        .nullIfEmpty()?.toBigDecimal()
    if (value == null) {
        return BigDecimal.ZERO
    }
    return value.scaleAndRoundAmount()
}

fun Row.getBigDecimalFromPercent(cellName: String, scale: Int): BigDecimal? {
    val value = getBigDecimal(cellName)?.toDouble() ?: return null
    return (value * 100).toBigDecimal().scaleAndRoundAmount()
}

fun Row.getInt(cellName: String): Int? {
    val cell = getCellByName(cellName) ?: return null
    if (cell.cellType == CellType.NUMERIC) {
        return cell.numericCellValue.toInt()
    }

    return this.getString(cellName)
        ?.filter { it.isDigit() }
        ?.nullIfEmpty()
        ?.toInt()
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

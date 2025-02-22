package ru.nemodev.insightestate.service.estate

import de.siegmar.fastcsv.writer.CsvWriter
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import ru.nemodev.insightestate.config.property.AppProperties
import ru.nemodev.insightestate.entity.*
import ru.nemodev.insightestate.repository.EstateRepository
import ru.nemodev.platform.core.api.domen.file.FileData
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.NotFoundLogicalException
import ru.nemodev.platform.core.extensions.nullIfEmpty
import ru.nemodev.platform.core.integration.s3.minio.config.S3MinioProperties
import java.io.ByteArrayInputStream
import java.io.StringWriter
import java.math.BigDecimal
import java.util.*
import kotlin.jvm.optionals.getOrNull

interface EstateService {

    fun findAll(
        types: Set<EstateType>?,
        buildEndYears: Set<Int>?,
        rooms: Set<String>?,
        price: String?,
        grades: Set<String>?,
        beachTravelTimes: Set<String>?,
        airportTravelTimes: Set<String>?,
        parking: Boolean?,
        pageable: Pageable
    ): List<EstateEntity>

    fun findById(
        id: UUID
    ): EstateEntity

    fun downloadCsvFile(): FileData
}

@Service
class EstateServiceImpl (
    private val appProperties: AppProperties,
    private val s3MinioProperties: S3MinioProperties,
    private val repository: EstateRepository,
) : EstateService {

    override fun findAll(
        types: Set<EstateType>?,
        buildEndYears: Set<Int>?,
        rooms: Set<String>?,
        price: String?,
        grades: Set<String>?,
        beachTravelTimes: Set<String>?,
        airportTravelTimes: Set<String>?,
        parking: Boolean?,
        pageable: Pageable
    ): List<EstateEntity> {

        val minPrice = when (price) {
            "1" -> BigDecimal.ZERO
            "2" -> BigDecimal.valueOf(100_000)
            "3" -> BigDecimal.valueOf(200_000)
            "4" -> BigDecimal.valueOf(500_000)
            "5" -> BigDecimal.valueOf(1_000_000)
            else -> null
        }
        val maxPrice = when (price) {
            "1" -> BigDecimal.valueOf(100_000)
            "2" -> BigDecimal.valueOf(200_000)
            "3" -> BigDecimal.valueOf(500_000)
            "4" -> BigDecimal.valueOf(1_000_000)
            "5" -> BigDecimal.valueOf(100_000_000_000) // =)
            else -> null
        }

        val estateList = repository.findByParams(
            types = types?.map { it.name }?.toTypedArray(),
            buildEndYears = buildEndYears?.map { it.toString() }?.toTypedArray(),

            isStudioRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("0"),
            isOneRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("1"),
            isTwoRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("2"),
            isFreeRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("3"),
            isFourRoom = if (rooms.isNullOrEmpty()) null else rooms.contains("4"),

            minPrice = minPrice,
            maxPrice = maxPrice,

            gradeInvestmentSecurity = if (grades.isNullOrEmpty()) null else if (grades.contains("1")) BigDecimal.valueOf(9) else null,
            gradeInvestmentPotential = if (grades.isNullOrEmpty()) null else if (grades.contains("2")) BigDecimal.valueOf(9) else null,
            gradeProjectLocation = if (grades.isNullOrEmpty()) null else if (grades.contains("3")) BigDecimal.valueOf(9) else null,
            gradeComfortOfLife = if (grades.isNullOrEmpty()) null else if (grades.contains("4")) BigDecimal.valueOf(9) else null,

            maxBeachWalkTravelTimeOne = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("1")) 5 else null,
            minBeachWalkTravelTimeTwo = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("2")) 6 else null,
            maxBeachWalkTravelTimeTwo = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("2")) 10 else null,
            minBeachWalkTravelTimeFree = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("3")) 11 else null,
            maxBeachWalkTravelTimeFree = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("3")) 30 else null,

            maxBeachCarTravelTimeOne = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("11")) 5 else null,
            minBeachCarTravelTimeTwo = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("12")) 6 else null,
            maxBeachCarTravelTimeTwo = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("12")) 10 else null,
            minBeachCarTravelTimeFree = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("13")) 11 else null,
            maxBeachCarTravelTimeFree = if (beachTravelTimes.isNullOrEmpty()) null else if (beachTravelTimes.contains("13")) 30 else null,

            maxAirportCarTravelTimeOne = if (airportTravelTimes.isNullOrEmpty()) null else if (airportTravelTimes.contains("1")) 30 else null,
            minAirportCarTravelTimeTwo = if (airportTravelTimes.isNullOrEmpty()) null else if (airportTravelTimes.contains("2")) 31 else null,
            maxAirportCarTravelTimeTwo = if (airportTravelTimes.isNullOrEmpty()) null else if (airportTravelTimes.contains("2")) 60 else null,
            maxAirportCarTravelTimeFree = if (airportTravelTimes.isNullOrEmpty()) null else if (airportTravelTimes.contains("3")) 61 else null,

            parking = parking,

            limit = pageable.pageSize,
            offset = pageable.offset
        )

        return estateList
    }

    override fun findById(id: UUID): EstateEntity {
        val estateEntity = repository.findById(id).getOrNull()
            ?: throw NotFoundLogicalException(errorCode = ErrorCode.createNotFound("Project object not found"))

        return estateEntity
    }

    // TODO вынести в отдельный класс
    override fun downloadCsvFile(): FileData {
        val estateList = repository.findAll()

        val stringWriter = StringWriter()
        val csvWriter = CsvWriter.builder()
            .build(stringWriter)
            .writeRecord(
                "Name",
                "Link (Auto)",
                "Collection ID",
                "Locale ID",
                "Item ID",
                "Created On",
                "Updated On",
                "Published On",
                "Name of the developer",
                "Main Score",
                "Score Investment security",
                "Score Investment potential",
                "Score Location",
                "Score Comfort",
                "Date of Delivery NEW",
                "Year",
                "Total Apartments",
                "City",
                "District",
                "Minutes to the beach NEW",
                "Minutes to the beach",
                "Minutes to the airport for filter",
                "Minutes to the airport",
                "Minutes to the Mall",
                "Object Class",
                "Project Types NEW",
                "Parking space NEW",
                "Parking space",
                "Number of floors",
                "Project Features",
                "Layout NEW",
                "Price from",
                "Price (студия)",
                "Price (1 спальня)",
                "Price (2 спальни)",
                "Price (3 спальни)",
                "Price (4+ спальни)",
                "Area (студия)",
                "Area (1 спальня)",
                "Area (2 спальни)",
                "Area (3 спальни)",
                "Area (4+ спальни)",
                "Cap Rate",
                "IRR",
                "ROI 10 year",
                "Link to map",
                "Embed to map",
                "Main Image",
                "Gallery Preview",
                "Gallery",
                "Image (Outdoor #2)",
                "Image (Indoor #1)",
                "Image (Indoor #2)",
                "Description of the object",
                "Description of the internal infrastructure",
                "Description of the area",
                "Project plan",
                "Price for filter",
                "Best Grage NEW"
        )

        estateList.forEach { estate ->
            val estateDetail = estate.estateDetail
            csvWriter.writeRecord(
                estateDetail.name,
                estateDetail.name.lowercase().replace(" ", "-"),
                "67aa5457ac73e442cea4bc4a",
                "672b5797ac1486cdfc512283",
                "67aa547aee7791faa7dc1a5f", // TODO у всех разный как заполнять?
                "Mon Feb 10 2025 19:33:14 GMT+0000 (Coordinated Universal Time)", // TODO как форматировать даты createdAt
                "Mon Feb 10 2025 19:33:14 GMT+0000 (Coordinated Universal Time)", // TODO updatedAt
                "Mon Feb 10 2025 19:33:14 GMT+0000 (Coordinated Universal Time)", // TODO updatedAt
                estateDetail.developer.name,
                estateDetail.grade.main.toString().replace(".", ","),
                estateDetail.grade.investmentSecurity.toString().replace(".", ","),
                estateDetail.grade.investmentPotential.toString().replace(".", ","),
                estateDetail.grade.projectLocation.toString().replace(".", ","),
                estateDetail.grade.comfortOfLife.toString().replace(".", ","),
                "Mon Mar 31 2025 00:00:00 GMT+0000 (Coordinated Universal Time", // TODO дата сдачи
                estateDetail.buildEndDate?.year?.toString(),
                estateDetail.unitCount.total.toString(),
                estateDetail.location.name,
                estateDetail.location.district,
                estateDetail.infrastructure.beachTime.toScv(),
                estateDetail.infrastructure.beachTime.car.toString(),
                estateDetail.infrastructure.airportTime.toAirportScv(),
                estateDetail.infrastructure.airportTime.car.toString(),
                estateDetail.infrastructure.mallTime.car.toString(),
                estateDetail.level.csv,
                estateDetail.type.csv,
                if (estateDetail.options.parkingSize != null) "est" else "net",
                estateDetail.options.parkingSize?.toString(),
                estateDetail.floors?.toString(),
                estateDetail.options.toCsv(),
                estateDetail.roomLayouts.toCsv(),
                estateDetail.price.min.toString(),
                estateDetail.roomLayouts.studio?.price?.toMinMaxCsv(),
                estateDetail.roomLayouts.one?.price?.toMinMaxCsv(),
                estateDetail.roomLayouts.two?.price?.toMinMaxCsv() ?: estateDetail.roomLayouts.villaTwo?.price?.toMinMaxCsv(),
                estateDetail.roomLayouts.three?.price?.toMinMaxCsv() ?: estateDetail.roomLayouts.villaThree?.price?.toMinMaxCsv(),
                estateDetail.roomLayouts.four?.price?.toMinMaxCsv()
                    ?: estateDetail.roomLayouts.villaFour?.price?.toMinMaxCsv()
                    ?: estateDetail.roomLayouts.four?.price?.toMinMaxCsv()
                    ?: estateDetail.roomLayouts.villaFour?.price?.toMinMaxCsv(),
                estateDetail.roomLayouts.studio?.square?.toMinMaxCsv(),
                estateDetail.roomLayouts.one?.square?.toMinMaxCsv(),
                estateDetail.roomLayouts.two?.square?.toMinMaxCsv() ?: estateDetail.roomLayouts.villaTwo?.square?.toMinMaxCsv(),
                estateDetail.roomLayouts.three?.square?.toMinMaxCsv() ?: estateDetail.roomLayouts.villaThree?.square?.toMinMaxCsv(),
                estateDetail.roomLayouts.four?.square?.toMinMaxCsv()
                    ?: estateDetail.roomLayouts.villaFour?.square?.toMinMaxCsv()
                    ?: estateDetail.roomLayouts.four?.square?.toMinMaxCsv()
                    ?: estateDetail.roomLayouts.villaFour?.square?.toMinMaxCsv(),
                estateDetail.profitability.capRateFirstYear.toPercentCsv(),
                estateDetail.profitability.irr.toPercentCsv(),
                estateDetail.profitability.roi.toPercentCsv(),
                estateDetail.location.mapUrl,
                estateDetail.location.mapUrl, // TODO где брать 2 ссылку как в примере?
                getMainImage(estate),   //   TODO как определить главную фотку?
                getGalleryPreviewImage(estate), // TODO как определить фото превью?
                getGalleryImages(estate), // TODO какой порядок у фото?
                null, // TODO где брать?
                null, // TODO где брать?
                null, // TODO где брать?
                estateDetail.shortDescriptionRu,
                null, // TODO где брать описание инфры?
                null, // TODO где брать описание окружения?
                null, // TODO где картинку плана?
                estateDetail.price.getPriceFilterCsv(),
                estateDetail.grade.toCsv()
            )
        }

        return FileData(
            name = "Insight Estate - Properties.csv",
            extension = "csv",
            mediaType = MediaType.parseMediaType("text/csv"),
            inputStream = ByteArrayInputStream(stringWriter.toString().toByteArray())
        )
    }

    private fun EstateGrade.toCsv(): String {
        val result = StringBuilder()
        if (investmentSecurity >= BigDecimal.valueOf(9)) result.append("samye-bezopasnye-dlya-investiciy; ")
        if (investmentPotential >= BigDecimal.valueOf(9)) result.append("naibolshaya-dohodnost; ")
        if (projectLocation >= BigDecimal.valueOf(9)) result.append("samye-udobnye-lokacii; ")
        if (comfortOfLife >= BigDecimal.valueOf(9)) result.append("samye-komfortnye-dlya-zhizni")

        return result.toString().trimEnd(';', ' ')
    }

    private fun MinMaxAvgParam.getPriceFilterCsv(): String {
        val result = StringBuilder()
        if (min.toInt() < 100_000) result.append("100-000; ")
        if (min.toInt() in 100_000..200_000) result.append("100-000----200-000; ")
        if (min.toInt() in 200_000..500_000) result.append("200-000----500-000; ")
        if (min.toInt() in 500_000..1_000_000) result.append("500-000----1-000-000; ")
        if (min.toInt() >= 1_000_000) result.append("ot-1-000-000")

        return result.toString().trimEnd(';', ' ')
    }

    private fun getMainImage(estateEntity: EstateEntity): String? {
        val mainImage = estateEntity.estateDetail.facilityImages?.firstOrNull()
            ?: estateEntity.estateDetail.exteriorImages?.firstOrNull()
            ?: estateEntity.estateDetail.interiorImages?.firstOrNull()

        if (mainImage == null) return null

        return "${appProperties.imageBaseUrl}/${s3MinioProperties.bucket}/$mainImage"
    }

    private fun getGalleryPreviewImage(estateEntity: EstateEntity): String? {
        val galleryPreviewImage = estateEntity.estateDetail.exteriorImages?.firstOrNull()
            ?: estateEntity.estateDetail.interiorImages?.firstOrNull()

        if (galleryPreviewImage == null) return null

        return "${appProperties.imageBaseUrl}${s3MinioProperties.bucket}/$galleryPreviewImage"
    }

    private fun getGalleryImages(estateEntity: EstateEntity): String? {
        val result = StringBuilder()
        estateEntity.estateDetail.facilityImages?.forEach {
            result.append("${appProperties.imageBaseUrl}${s3MinioProperties.bucket}/$it; ")
        }
        estateEntity.estateDetail.exteriorImages?.forEach {
            result.append("${appProperties.imageBaseUrl}${s3MinioProperties.bucket}/$it; ")
        }
        estateEntity.estateDetail.interiorImages?.forEach {
            result.append("${appProperties.imageBaseUrl}${s3MinioProperties.bucket}/$it; ")
        }

        return result.toString().trimEnd(';', ' ').nullIfEmpty()
    }

    private fun BigDecimal.toPercentCsv(): String {
        return toString().replace(".", ",") + "%"
    }

    private fun MinMaxAvgParam.toMinMaxCsv(): String {
        return "$min — $max"
    }

    private fun RoomLayouts.toCsv(): String? {
        val result = StringBuilder()
        if (studio != null) result.append("ctudiya; ")
        if (one != null) result.append("1-spalnya; ")
        if (two != null || villaTwo != null) result.append("2-spalni; ")
        if (three != null || villaThree != null) result.append("3-spalni; ")
        if (four != null || five != null || villaFour != null || villaFive != null) result.append("4-spalni; ")

        return result.toString().trimEnd(';', ' ').nullIfEmpty()
    }

    private fun EstateOptions.toCsv(): String? {
        val result = StringBuilder()
        if (gym) result.append("gym-fitness-center; ")
        if (childRoom) result.append("childrens-playground; ")
        if (shop) result.append("grocery; ")
        if (coworking) result.append("coworking; ")

        return result.toString().trimEnd(';', ' ').nullIfEmpty()
    }

    private fun TravelTime.toScv(): String? {
        val walkString =
            when (walk) {
                null -> null
                in 0..5 -> "menee-5-min-peshkom; "
                in 6..10 -> "6-10-min-peshkom; "
                in 11..30 -> "11-30-min-peshkom; "
                else -> null
            }

        val carString =
            when (car) {
                in 0..5 -> "menee-5-min-na-mashine; "
                in 6..10 -> "6-10-min-na-mashine; "
                in 11..30 -> "11-30-min-na-mashine; "
                else -> null
            }

        val result = StringBuilder()
        if (walkString != null) {
            result.append(walkString)
        }
        if (carString != null) {
            result.append(carString)
        }

        return result.toString().trimEnd(';', ' ').nullIfEmpty()
    }

    private fun TravelTime.toAirportScv(): String? {
        return when (car) {
            in 0..30 -> "do-30-min-na-mashine"
            in 31..60 -> "do-60-min-na-mashine"
            in 61..10000 -> "60-min-na-mashine"
            else -> null
        }
    }
}

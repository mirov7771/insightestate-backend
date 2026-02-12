package ru.nemodev.insightestate.api.client.v1.processor

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import ru.nemodev.insightestate.api.auth.v1.dto.CustomPageDtoRs
import ru.nemodev.insightestate.api.client.v1.converter.EstateDetailDtoRsConverter
import ru.nemodev.insightestate.api.client.v1.converter.EstateDtoRsConverter
import ru.nemodev.insightestate.api.client.v1.converter.formatDate
import ru.nemodev.insightestate.api.client.v1.dto.estate.*
import ru.nemodev.insightestate.api.client.v1.dto.user.MainInfoDto
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.insightestate.entity.EstateType
import ru.nemodev.insightestate.entity.UnitEntity
import ru.nemodev.insightestate.integration.currency.CurrencyService
import ru.nemodev.insightestate.repository.UnitRepository
import ru.nemodev.insightestate.service.estate.EstateImageLoader
import ru.nemodev.insightestate.service.estate.EstateLoader
import ru.nemodev.insightestate.service.estate.EstateService
import ru.nemodev.insightestate.utils.OrderBy
import ru.nemodev.platform.core.extensions.isNotNullOrEmpty
import ru.nemodev.platform.core.extensions.nullIfEmpty
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

interface EstateProcessor {

    fun findAll(
        currency: String? = null,
        types: Set<EstateType>?,
        buildEndYears: Set<Int>?,
        rooms: Set<String>?,
        price: String?,
        grades: Set<String>?,
        beachTravelTimes: Set<String>?,
        airportTravelTimes: Set<String>?,
        parking: Boolean?,
        managementCompanyEnabled: Boolean?,
        beachName: Set<String>?,
        city: Set<String>?,
        minPrice: BigDecimal? = null,
        maxPrice: BigDecimal? = null,
        pageable: Pageable,
        userId: UUID? = null,
        name: String? = null,
        developer: Set<String>?,
        petFriendly: Boolean?,
        units: Set<String>?,
        sizeMin: Long? = null,
        sizeMax: Long? = null,
        orderBy: OrderBy,
        eia: Boolean? = null,
        landPurchased: Boolean? = null,
    ): CustomPageDtoRs

    fun findById(
        currency: String? = null,
        id: UUID
    ): EstateDetailDtoRs

    fun loadFromFile(filePart: MultipartFile)
    fun loadFromGoogle()
    fun loadImagesFromDir()
    fun loadImagesFromGoogleDrive()

    fun aiRequest(
        currency: String? = null,
        rq: AiRequest
    ): CustomPageDtoRs
    fun geo(
        currency: String? = null,
    ): GeoRs
    fun findUnits(
        currency: String? = null,
        id: UUID,
        orderBy: String?,
        rooms: Set<String>?,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        minSize: Double? = null,
        maxSize: Double? = null,
        minPriceSq: Double? = null,
        maxPriceSq: Double? = null
    ): UnitsRs
    fun getMainInfo(userId: UUID): MainInfoDto

    fun prepareXml(): String
    fun prepareJson(): List<EstateEntity>
    fun prepareJsonUnit(): List<UnitEntity>
}

@Component
class EstateProcessorImpl(
    private val estateService: EstateService,
    private val estateDtoRsConverter: EstateDtoRsConverter,
    private val estateDetailDtoRsConverter: EstateDetailDtoRsConverter,

    private val estateLoader: EstateLoader,
    private val estateImageLoader: EstateImageLoader,
    private val unitRepository: UnitRepository,

    private val currencyService: CurrencyService,
) : EstateProcessor {

    companion object {
        private val DECIMAL_SYMBOLS = DecimalFormatSymbols(Locale.US)
        val dec = DecimalFormat("#,###", DECIMAL_SYMBOLS)
    }

    override fun findAll(
        currency: String?,
        types: Set<EstateType>?,
        buildEndYears: Set<Int>?,
        rooms: Set<String>?,
        price: String?,
        grades: Set<String>?,
        beachTravelTimes: Set<String>?,
        airportTravelTimes: Set<String>?,
        parking: Boolean?,
        managementCompanyEnabled: Boolean?,
        beachName: Set<String>?,
        city: Set<String>?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        pageable: Pageable,
        userId: UUID?,
        name: String?,
        developer: Set<String>?,
        petFriendly: Boolean?,
        units: Set<String>?,
        sizeMin: Long?,
        sizeMax: Long?,
        orderBy: OrderBy,
        eia: Boolean?,
        landPurchased: Boolean?
    ): CustomPageDtoRs {
        var rMinPrice = minPrice
        var rMaxPrice = maxPrice
        if (currency != null && currency != "THB") {
            val rate = currencyService.getRate(currency)
            if (rMinPrice != null && rMinPrice > BigDecimal.ZERO) {
                rMinPrice = rMinPrice.divide(rate, 2, RoundingMode.HALF_UP)
            }
            if (rMaxPrice != null && rMaxPrice > BigDecimal.ZERO) {
                rMaxPrice = rMaxPrice.divide(rate, 2, RoundingMode.HALF_UP)
            }
        }

        var unitCountMin: Int? = null
        var unitCountMax: Int? = null

        if (units.isNotNullOrEmpty()) {
            if (units!!.contains("5"))
                unitCountMin = 500
            if (units.contains("4"))
                unitCountMin = 200
            if (units.contains("3"))
                unitCountMin = 50
            if (units.contains("2"))
                unitCountMin = 10
            if (units.contains("1"))
                unitCountMin = 0

            if (units.contains("1"))
                unitCountMax = 10
            if (units.contains("2"))
                unitCountMax = 50
            if (units.contains("3"))
                unitCountMax = 200
            if (units.contains("4"))
                unitCountMax = 500
            if (units.contains("4"))
                unitCountMax = 50000
        }

        var minSize = sizeMin
        if (minSize == null && sizeMax != null) {
            minSize = 0
        }

        val estates = estateService.findAll(
            types = types,
            buildEndYears = buildEndYears,
            rooms = rooms,
            price = price,
            grades = grades,
            beachTravelTimes = beachTravelTimes,
            airportTravelTimes = airportTravelTimes,
            parking = parking,
            managementCompanyEnabled = managementCompanyEnabled,
            beachName = beachName,
            city = city,
            minPrice = rMinPrice,
            maxPrice = rMaxPrice,
            pageable = pageable,
            userId = userId,
            name = name,
            developer = developer,
            petFriendly = petFriendly,
            unitCountMin = unitCountMin,
            unitCountMax = unitCountMax,
            sizeMin = minSize,
            sizeMax = sizeMax,
            eia = eia,
            landPurchased = landPurchased
        )

        if (currency != null && currency != "THB") {
            estates.forEach { entity ->
                entity.estateDetail.price.min = getPrice(entity.estateDetail.price.min, currency)!!
                entity.estateDetail.price.max = getPrice(entity.estateDetail.price.max, currency)!!
                entity.estateDetail.price.avg = getPrice(entity.estateDetail.price.avg, currency)
            }
        }

        val count = estateService.findCount(
            types = types,
            buildEndYears = buildEndYears,
            rooms = rooms,
            price = price,
            grades = grades,
            beachTravelTimes = beachTravelTimes,
            airportTravelTimes = airportTravelTimes,
            parking = parking,
            managementCompanyEnabled = managementCompanyEnabled,
            beachName = beachName,
            city = city,
            minPrice = rMinPrice,
            maxPrice = rMaxPrice,
            name = name,
            developer = developer,
            petFriendly = petFriendly,
            unitCountMin = unitCountMin,
            unitCountMax = unitCountMax,
            sizeMin = minSize,
            sizeMax = sizeMax,
            eia = eia,
            landPurchased = landPurchased,
        )

        var list = estates.map { estateDtoRsConverter.convert(it) }

        val chunkedList = when (orderBy) {
            OrderBy.PRICE_ASC -> list.sortedBy { it.priceMin }
            OrderBy.PRICE_DESC -> list.sortedByDescending { it.priceMin }
            OrderBy.SIZE_ASC -> list.sortedBy { it.sizeMin }
            OrderBy.SIZE_DESC -> list.sortedByDescending { it.sizeMin }
            else -> list.sortedByDescending { it.updatedAt }
        }.chunked(pageable.pageSize)

        if (chunkedList.size > pageable.pageNumber)
            list = chunkedList[pageable.pageNumber]

        return CustomPageDtoRs(
            items = list,
            pageSize = list.size,
            pageNumber = pageable.pageNumber,
            hasMore = list.size >= pageable.pageSize,
            totalPages = estateService.findPages(pageable.pageSize, count),
            totalCount = count
        )
    }

    override fun findById(currency: String?, id: UUID): EstateDetailDtoRs {
        val entity = estateService.findById(id)
        entity.estateDetail.price.min = getPrice(entity.estateDetail.price.min, currency)!!
        entity.estateDetail.price.max = getPrice(entity.estateDetail.price.max, currency)!!
        entity.estateDetail.price.avg = getPrice(entity.estateDetail.price.avg, currency)

        if (entity.estateDetail.roomLayouts.one?.price != null) {
            entity.estateDetail.roomLayouts.one?.price!!.min = getPrice(entity.estateDetail.roomLayouts.one?.price!!.min, currency)!!
            entity.estateDetail.roomLayouts.one?.price!!.max = getPrice(entity.estateDetail.roomLayouts.one?.price!!.max, currency)!!
            if (entity.estateDetail.roomLayouts.one?.price?.avg != null)
                entity.estateDetail.roomLayouts.one?.price!!.avg = getPrice(entity.estateDetail.roomLayouts.one?.price?.avg, currency)
        }
        if (entity.estateDetail.roomLayouts.two?.price != null) {
            entity.estateDetail.roomLayouts.two?.price!!.min = getPrice(entity.estateDetail.roomLayouts.two?.price!!.min, currency)!!
            entity.estateDetail.roomLayouts.two?.price!!.max = getPrice(entity.estateDetail.roomLayouts.two?.price!!.max, currency)!!
            if (entity.estateDetail.roomLayouts.two?.price?.avg != null)
                entity.estateDetail.roomLayouts.two?.price!!.avg = getPrice(entity.estateDetail.roomLayouts.two?.price?.avg, currency)
        }
        if (entity.estateDetail.roomLayouts.three?.price != null) {
            entity.estateDetail.roomLayouts.three?.price!!.min = getPrice(entity.estateDetail.roomLayouts.three?.price!!.min, currency)!!
            entity.estateDetail.roomLayouts.three?.price!!.max = getPrice(entity.estateDetail.roomLayouts.three?.price!!.max, currency)!!
            if (entity.estateDetail.roomLayouts.three?.price?.avg != null)
                entity.estateDetail.roomLayouts.three?.price!!.avg = getPrice(entity.estateDetail.roomLayouts.three?.price?.avg, currency)
        }
        if (entity.estateDetail.roomLayouts.four?.price != null) {
            entity.estateDetail.roomLayouts.four?.price!!.min = getPrice(entity.estateDetail.roomLayouts.four?.price!!.min, currency)!!
            entity.estateDetail.roomLayouts.four?.price!!.max = getPrice(entity.estateDetail.roomLayouts.four?.price!!.max, currency)!!
            if (entity.estateDetail.roomLayouts.four?.price?.avg != null)
                entity.estateDetail.roomLayouts.four?.price!!.avg = getPrice(entity.estateDetail.roomLayouts.four?.price?.avg, currency)
        }
        if (entity.estateDetail.roomLayouts.five?.price != null) {
            entity.estateDetail.roomLayouts.five?.price!!.min = getPrice(entity.estateDetail.roomLayouts.five?.price!!.min, currency)!!
            entity.estateDetail.roomLayouts.five?.price!!.max = getPrice(entity.estateDetail.roomLayouts.five?.price!!.max, currency)!!
            if (entity.estateDetail.roomLayouts.five?.price?.avg != null)
                entity.estateDetail.roomLayouts.five?.price!!.avg = getPrice(entity.estateDetail.roomLayouts.five?.price?.avg, currency)
        }
        if (entity.estateDetail.roomLayouts.studio?.price != null) {
            entity.estateDetail.roomLayouts.studio?.price!!.min = getPrice(entity.estateDetail.roomLayouts.studio?.price!!.min, currency)!!
            entity.estateDetail.roomLayouts.studio?.price!!.max = getPrice(entity.estateDetail.roomLayouts.studio?.price!!.max, currency)!!
            if (entity.estateDetail.roomLayouts.studio?.price?.avg != null)
                entity.estateDetail.roomLayouts.studio?.price!!.avg = getPrice(entity.estateDetail.roomLayouts.studio?.price?.avg, currency)
        }
        if (entity.estateDetail.roomLayouts.villaTwo?.price != null) {
            entity.estateDetail.roomLayouts.villaTwo?.price!!.min = getPrice(entity.estateDetail.roomLayouts.villaTwo?.price!!.min, currency)!!
            entity.estateDetail.roomLayouts.villaTwo?.price!!.max = getPrice(entity.estateDetail.roomLayouts.villaTwo?.price!!.max, currency)!!
            if (entity.estateDetail.roomLayouts.villaTwo?.price?.avg != null)
                entity.estateDetail.roomLayouts.villaTwo?.price!!.avg = getPrice(entity.estateDetail.roomLayouts.villaTwo?.price?.avg, currency)
        }
        if (entity.estateDetail.roomLayouts.villaThree?.price != null) {
            entity.estateDetail.roomLayouts.villaThree?.price!!.min = getPrice(entity.estateDetail.roomLayouts.villaThree?.price!!.min, currency)!!
            entity.estateDetail.roomLayouts.villaThree?.price!!.max = getPrice(entity.estateDetail.roomLayouts.villaThree?.price!!.max, currency)!!
            if (entity.estateDetail.roomLayouts.villaThree?.price?.avg != null)
                entity.estateDetail.roomLayouts.villaThree?.price!!.avg = getPrice(entity.estateDetail.roomLayouts.villaThree?.price?.avg, currency)
        }
        if (entity.estateDetail.roomLayouts.villaFour?.price != null) {
            entity.estateDetail.roomLayouts.villaFour?.price!!.min = getPrice(entity.estateDetail.roomLayouts.villaFour?.price!!.min, currency)!!
            entity.estateDetail.roomLayouts.villaFour?.price!!.max = getPrice(entity.estateDetail.roomLayouts.villaFour?.price!!.max, currency)!!
            if (entity.estateDetail.roomLayouts.villaFour?.price?.avg != null)
                entity.estateDetail.roomLayouts.villaFour?.price!!.avg = getPrice(entity.estateDetail.roomLayouts.villaFour?.price?.avg, currency)
        }
        if (entity.estateDetail.roomLayouts.villaFive?.price != null) {
            entity.estateDetail.roomLayouts.villaFive?.price!!.min = getPrice(entity.estateDetail.roomLayouts.villaFive?.price!!.min, currency)!!
            entity.estateDetail.roomLayouts.villaFive?.price!!.max = getPrice(entity.estateDetail.roomLayouts.villaFive?.price!!.max, currency)!!
            if (entity.estateDetail.roomLayouts.villaFive?.price?.avg != null)
                entity.estateDetail.roomLayouts.villaFive?.price!!.avg = getPrice(entity.estateDetail.roomLayouts.villaFive?.price?.avg, currency)
        }

        val rs = estateDetailDtoRsConverter.convert(entity)
        return rs
    }

    override fun loadFromFile(filePart: MultipartFile) {
        estateLoader.loadFromFile(filePart)
    }

    override fun loadFromGoogle() {
        estateLoader.loadFromGoogleSpreadsheets()
    }

    override fun loadImagesFromDir() {
        estateImageLoader.loadFromDir()
    }

    override fun loadImagesFromGoogleDrive() {
        estateImageLoader.loadFromGoogleDrive()
    }

    override fun aiRequest(currency: String?, rq: AiRequest): CustomPageDtoRs {
        val estates = estateService.aiRequest(
            String(Base64.getDecoder().decode(rq.request))
        )
        if (currency != "THB") {
            estates.forEach { entity ->
                entity.estateDetail.price.min = getPrice(entity.estateDetail.price.min, currency)!!
                entity.estateDetail.price.max = getPrice(entity.estateDetail.price.max, currency)!!
                entity.estateDetail.price.avg = getPrice(entity.estateDetail.price.avg, currency)
            }
        }
        return CustomPageDtoRs(
            items = estates.map { estateDtoRsConverter.convert(it) },
            pageSize = estates.size,
            pageNumber = 1,
            hasMore = false,
            totalPages = 0
        )
    }

    override fun geo(currency: String?): GeoRs {
        val currencySym = when (currency) {
            "RUB" -> "₽"
            "THB" -> "฿"
            "AUD" -> "A$"
            else -> "$"
        }
        val list = estateService.findAll().map {
            var image = if (it.estateDetail.exteriorImages.isNotNullOrEmpty())
                it.estateDetail.exteriorImages!![0]
            else if (it.estateDetail.interiorImages.isNotNullOrEmpty())
                it.estateDetail.interiorImages!![0]
            else if (it.estateDetail.facilityImages.isNotNullOrEmpty())
                it.estateDetail.facilityImages!![0]
            else
                null
            if (image.isNotNullOrEmpty()) {
                image = "https://lotsof.properties/estate-images/$image"
            }
            var roi = "ROI ${it.estateDetail.profitability.roi}%"
            if (it.estateDetail.buildEndDate != null) {
                roi = "${formatDate(it.estateDetail.buildEndDate.toString())} • $roi"
            }
            GeoDto(
                id = it.id,
                lat = it.estateDetail.lat ?: getLat(it.estateDetail.location.mapUrl),
                lng = it.estateDetail.lon ?: getLng(it.estateDetail.location.mapUrl),
                title = it.estateDetail.name,
                image = image,
                description = "${dec.format(getPrice(it.estateDetail.price.min, currency))} ${currencySym} • ${it.estateDetail.location.city}",
                toolTip1 = if (it.estateDetail.toolTip1.isNullOrEmpty()) "false" else "true",
                toolTip2 = if (it.estateDetail.toolTip2.isNullOrEmpty()) "false" else "true",
                toolTip3 = if (it.estateDetail.toolTip3.isNullOrEmpty()) "false" else "true",
                roi = roi
            )
        }
        return GeoRs(
            geo = list.filter { it.lat != null && it.lng != null },
        )
    }

    override fun findUnits(
        currency: String?,
        id: UUID,
        orderBy: String?,
        rooms: Set<String>?,
        minPrice: Double?,
        maxPrice: Double?,
        minSize: Double?,
        maxSize: Double?,
        minPriceSq: Double?,
        maxPriceSq: Double?
    ): UnitsRs {

        val estate = estateService.findById(id)
        val projectId = "${estate.estateDetail.projectId}%"

        val rawUnits = unitRepository.findByProjectId(projectId)

        data class UnitWithNumbers(
            val unit: UnitEntity,
            val price: Double,
            val priceSq: Double,
            val square: Double
        )

        val unitsWithNumbers = rawUnits.map { unit ->
            val priceValue = if (currency != null && currency != "THB")
                getPrice(strToBigDecimal(unit.price), currency)?.toDouble() ?: 0.0
            else stringToDouble(unit.price)

            val priceSqValue = if (currency != null && currency != "THB")
                getPrice(strToBigDecimal(unit.priceSq), currency)?.toDouble() ?: 0.0
            else stringToDouble(unit.priceSq)

            val squareValue = stringToDouble(unit.square)

            UnitWithNumbers(unit, priceValue, priceSqValue, squareValue)
        }

        var units = unitsWithNumbers
        val counts = units.size

        if (units.isNotEmpty()) {

            if (orderBy != null) {
                units = when (orderBy.lowercase()) {
                    "price" -> units.sortedBy { it.price }
                    "area" -> units.sortedBy { it.square }
                    "income" -> units.sortedBy { it.priceSq }
                    "payback" -> units.sortedBy { it.unit.rooms }
                    else -> units
                }
            }

            if (rooms.isNotNullOrEmpty()) {
                units = units.filter {
                    it.unit.rooms != null && rooms!!.contains(it.unit.rooms!!.lowercase())
                }
            }

            if (minPrice != null) units = units.filter { it.price >= minPrice }
            if (maxPrice != null) units = units.filter { it.price <= maxPrice }
            if (minSize != null) units = units.filter { it.square >= minSize }
            if (maxSize != null) units = units.filter { it.square <= maxSize }
            if (minPriceSq != null) units = units.filter { it.priceSq >= minPriceSq }
            if (maxPriceSq != null) units = units.filter { it.priceSq <= maxPriceSq }
        }

        val resultUnits = units.map { uwn ->
            uwn.unit.apply {
                price = dec.format(uwn.price)
                priceSq = dec.format(uwn.priceSq)
            }
        }

        return UnitsRs(
            id = estate.id,
            name = estate.estateDetail.name,
            images = estate.estateDetail.exteriorImages
                ?: estate.estateDetail.facilityImages
                ?: estate.estateDetail.interiorImages,
            items = resultUnits,
            count = counts
        )
    }

    private fun stringToDouble(value: String?): Double {
        if (value == null) {
            return 0.0
        }
        return try {
            value.replace(" ", "")
                .replace(" ", "")
                .replace(",", ".")
                .toDouble()
        } catch (_: Exception) {
            0.0
        }
    }

    override fun getMainInfo(userId: UUID): MainInfoDto {
        val list = estateService.findAll().filter { it.isCanShow() }
        val units = unitRepository.findAll().toList()
        return MainInfoDto(
            collections = unitRepository.findAllCollections(userId.toString()),
            units = units.size,
            objects = list.size,
            bestObjects = list.filter { it.estateDetail.grade.main >= BigDecimal(9) }.size,
        )
    }

    override fun prepareXml(): String {
        return estateService.createXml()
    }

    override fun prepareJson(): List<EstateEntity> {
        val list = estateService.findAll()
        list.forEach {
            if (it.estateDetail.interiorImages.isNotNullOrEmpty()) {
                it.estateDetail.interiorImages = it.estateDetail.interiorImages!!.map { image ->
                    "https://lotsof.properties/estate-images/${image}"
                }.toMutableList()
            }
            if (it.estateDetail.facilityImages.isNotNullOrEmpty()) {
                it.estateDetail.facilityImages = it.estateDetail.facilityImages!!.map { image ->
                    "https://lotsof.properties/estate-images/${image}"
                }.toMutableList()
            }
            if (it.estateDetail.exteriorImages.isNotNullOrEmpty()) {
                it.estateDetail.exteriorImages = it.estateDetail.exteriorImages!!.map { image ->
                    "https://lotsof.properties/estate-images/${image}"
                }.toMutableList()
            }
        }
        return list
    }

    override fun prepareJsonUnit(): List<UnitEntity> {
        val units = unitRepository.findAll().toList()
        return units
    }

    private fun getLat(url: String): String? {
        val split = url.split("@")
        if (split.size < 2)
            return null
        val s = split[1].split(",")
        if (s.size < 2)
            return null
        return s[0]
    }

    private fun getLng(url: String): String? {
        val split = url.split("@")
        if (split.size < 2)
            return null
        val s = split[1].split(",")
        if (s.size < 2)
            return null
        return s[1]
    }

    private fun getPrice(
        price: BigDecimal?,
        currency: String?
    ): BigDecimal? {
        if (price == null)
            return null
        return currencyService.getValueByCurrency(
            value = price,
            currency = currency ?: "THB"
        )
    }

    private fun strToBigDecimal(str: String?): BigDecimal? {
        return str?.replace(" ", "")
            ?.replace(",", ".")
            ?.replace("%", "")
            ?.replace(" ", "")
            ?.nullIfEmpty()
            ?.toBigDecimal()
    }
}

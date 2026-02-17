package ru.nemodev.insightestate.service.airtable

import org.springframework.stereotype.Component
import ru.nemodev.insightestate.entity.*
import ru.nemodev.insightestate.extension.getBigDecimal
import ru.nemodev.insightestate.integration.airtable.dto.EstateRecordsDtoRs.AirtableRecordDto.AirtableProjectFieldsDto
import ru.nemodev.platform.core.extensions.nullIfEmpty
import ru.nemodev.platform.core.extensions.scaleAndRoundAmount
import java.math.BigDecimal

interface EstateAirtableMapper {
    fun mapToEntity(airtableDto: AirtableProjectFieldsDto): EstateEntity
}

@Component
class EstateAirtableMapperImpl : EstateAirtableMapper {

    override fun mapToEntity(airtableDto: AirtableProjectFieldsDto): EstateEntity {
        return EstateEntity(
            estateDetail = EstateDetail(
                // не используются
                projectId = airtableDto.projectId,
                name = airtableDto.projectName,
                shortDescriptionRu = airtableDto.descriptionRu,
                shortDescriptionEn = airtableDto.descriptionEn,
                landPurchased = when (airtableDto.landPurchased) {
                    "Да" -> true
                    "Нет" -> false
                    else -> null
                },
                eiaEnabled = when (airtableDto.hasEia) {
                    "Да" -> true
                    "Нет" -> false
                    else -> null
                },
                EstateDeveloper(
                    name = airtableDto.developer,
                    phone = airtableDto.developerPhone,
                    email = airtableDto.developerEmail,
                    presentation = airtableDto.presentation,
                ),
                EstateGrade(
                    main = BigDecimal.ZERO,
                    investmentSecurity = BigDecimal.ZERO,
                    investmentPotential = BigDecimal.ZERO,
                    projectLocation = BigDecimal.ZERO,
                    comfortOfLife = BigDecimal.ZERO
                ),
                ProjectCount(
                    total = airtableDto.totalProjects,
                    build = airtableDto.countProjectsInConstruction,
                    finished = airtableDto.countCompletedProjects,
                    deviationFromDeadline = null, // todo Кирилл обещал добавить
                ),
                when (airtableDto.projectStatus) {
                    "Строится" -> EstateStatus.BUILD
                    "Сдан" -> EstateStatus.FINISHED
                    else -> EstateStatus.UNKNOWN
                },
                buildEndDate = airtableDto.buildEndDate,
                unitCount = UnitCount(
                    total = airtableDto.totalUnits,
                    sailed = airtableDto.sailedUnits,
                    available = airtableDto.availableUnits,
                ),
                soldOut = when (airtableDto.soldOut) {
                    "Да" -> true
                    else -> false
                },
                type = when (airtableDto.propertyType) {
                    "Villa" -> EstateType.VILLA
                    else -> EstateType.APARTMENT
                },
                level = when (airtableDto.estateLevel) {
                    "Премиум" -> EstateLevelType.PREMIUM
                    "Люкс" -> EstateLevelType.LUX
                    "Комфорт" -> EstateLevelType.COMFORT
                    else -> EstateLevelType.UNKNOWN
                },
                profitability = EstateProfitability(
                    roi = BigDecimal.ZERO,              // не используется
                    roiSummary = BigDecimal.ZERO,       // не используется
                    irr = BigDecimal.ZERO,              // не используется
                    capRateFirstYear = BigDecimal.ZERO, // не используется
                    guarantee = airtableDto.guaranteedIncomeYieldPercent,
                    guaranteedDeveloperIncome = when (airtableDto.guaranteedDeveloperIncome) {
                        "Да" -> true
                        "Нет" -> false
                        else -> null
                    }
                ),
                location = EstateLocation(
                    name = airtableDto.location,
                    district = airtableDto.district,
                    beach = airtableDto.beach,
                    mapUrl = "", // Не нужна, есть координаты
                    city = airtableDto.city
                ),
                infrastructure = EstateInfrastructure(
                    beachTime = TravelTime(
                        walk = airtableDto.walkToBeachMinutes,
                        car = airtableDto.driveToBeachMinutes
                    ),
                    airportTime = TravelTime(
                        walk = null,
                        car = airtableDto.airportMinutes
                    ),
                    mallTime = TravelTime(
                        walk = airtableDto.mallWalkMinutes,
                        car = airtableDto.mallDriveMinutes
                    ),
                ),
                options = EstateOptions(
                    parkingSize = airtableDto.parkingSize?.toIntOrNull()?.let { if (it == 0) null else it },
                    gym = when (airtableDto.sport) {
                        "Да" -> true
                        else -> false
                    },
                    childRoom = when (airtableDto.forChildren) {
                        "Да" -> true
                        else -> false
                    },
                    shop = when (airtableDto.shops) {
                        "Да" -> true
                        else -> false
                    },
                    entertainment = when (airtableDto.entertainment) {
                        "Да" -> true
                        else -> false
                    },
                    coworking = when (airtableDto.coworking) {
                        "Да" -> true
                        else -> false
                    },
                    petFriendly = when (airtableDto.petFriendly) {
                        "Да" -> true
                        else -> false
                    }
                ),
                managementCompany = ManagementCompany(
                    enabled = when (airtableDto.hasManagementCompany) {
                        "Да" -> true
                        else -> false
                    },
                ),
                price = MinMaxAvgParam(
                    min = getMinPrice(airtableDto),
                    max = getMaxPrice(airtableDto),
                    avg = null   // не используется
                ),
                floors = airtableDto.floors,
                roomLayouts = RoomLayouts(
                    studio = getRoomParams(
                        minPricePerM2 = airtableDto.minPricePerM2Studio,
                        maxPricePerM2 = airtableDto.maxPricePerM2Studio,
                        avgPricePerM2 = airtableDto.avgPricePerM2Studio,
                        minTotalPrice = airtableDto.minTotalPriceStudio,
                        maxTotalPrice = airtableDto.maxTotalPriceStudio,
                        avgTotalPrice = airtableDto.avgTotalPriceStudio,
                        minArea = airtableDto.minAreaStudio,
                        maxArea = airtableDto.maxAreaStudio,
                    ),
                    one = getRoomParams(
                        minPricePerM2 = airtableDto.minPricePerM21Br,
                        maxPricePerM2 = airtableDto.maxPricePerM21Br,
                        avgPricePerM2 = airtableDto.avgPricePerM21Br,
                        minTotalPrice = airtableDto.minTotalPrice1Br,
                        maxTotalPrice = airtableDto.maxTotalPrice1Br,
                        avgTotalPrice = airtableDto.avgTotalPrice1Br,
                        minArea = airtableDto.minArea1Br,
                        maxArea = airtableDto.maxArea1Br,
                    ),
                    two = getRoomParams(
                        minPricePerM2 = airtableDto.minPricePerM22Br,
                        maxPricePerM2 = airtableDto.maxPricePerM22Br,
                        avgPricePerM2 = airtableDto.avgPricePerM22Br,
                        minTotalPrice = airtableDto.minTotalPrice2Br,
                        maxTotalPrice = airtableDto.maxTotalPrice2Br,
                        avgTotalPrice = airtableDto.avgTotalPrice2Br,
                        minArea = airtableDto.minArea2Br,
                        maxArea = airtableDto.maxArea2Br,
                    ),
                    three = getRoomParams(
                        minPricePerM2 = airtableDto.minPricePerM23Br,
                        maxPricePerM2 = airtableDto.maxPricePerM23Br,
                        avgPricePerM2 = airtableDto.avgPricePerM23Br,
                        minTotalPrice = airtableDto.minTotalPrice3Br,
                        maxTotalPrice = airtableDto.maxTotalPrice3Br,
                        avgTotalPrice = airtableDto.avgTotalPrice3Br,
                        minArea = airtableDto.minArea3Br,
                        maxArea = airtableDto.maxArea3Br,
                    ),
                    four = getRoomParams(
                        minPricePerM2 = airtableDto.minPricePerM24Br,
                        maxPricePerM2 = airtableDto.maxPricePerM24Br,
                        avgPricePerM2 = airtableDto.avgPricePerM24Br,
                        minTotalPrice = airtableDto.minTotalPrice4Br,
                        maxTotalPrice = airtableDto.maxTotalPrice4Br,
                        avgTotalPrice = airtableDto.avgTotalPrice4Br,
                        minArea = airtableDto.minArea4Br,
                        maxArea = airtableDto.maxArea4Br,
                    ),
                    five = getRoomParams(
                        minPricePerM2 = airtableDto.minPricePerM25Br,
                        maxPricePerM2 = airtableDto.maxPricePerM25Br,
                        avgPricePerM2 = airtableDto.avgPricePerM25Br,
                        minTotalPrice = airtableDto.minTotalPrice5Br,
                        maxTotalPrice = airtableDto.maxTotalPrice5Br,
                        avgTotalPrice = airtableDto.avgTotalPrice5Br,
                        minArea = airtableDto.minArea5Br,
                        maxArea = airtableDto.maxArea5Br,
                    ),
                    villaTwo = getRoomParams(
                        minPricePerM2 = airtableDto.minPricePerM2Villa,
                        maxPricePerM2 = airtableDto.maxPricePerM2Villa,
                        avgPricePerM2 = airtableDto.avgPricePerM2Villa,
                        minTotalPrice = airtableDto.minTotalPriceVilla2Br,
                        maxTotalPrice = airtableDto.maxTotalPriceVilla2Br,
                        avgTotalPrice = airtableDto.avgTotalPriceVilla,
                        minArea = airtableDto.minAreaVilla2Br,
                        maxArea = airtableDto.maxAreaVilla2Br,
                    ),
                    villaThree = getRoomParams(
                        minPricePerM2 = airtableDto.minPricePerM2Villa,
                        maxPricePerM2 = airtableDto.maxPricePerM2Villa,
                        avgPricePerM2 = airtableDto.avgPricePerM2Villa,
                        minTotalPrice = airtableDto.minTotalPriceVilla3Br,
                        maxTotalPrice = airtableDto.maxTotalPriceVilla3Br,
                        avgTotalPrice = airtableDto.avgTotalPriceVilla,
                        minArea = airtableDto.minAreaVilla3Br,
                        maxArea = airtableDto.maxAreaVilla3Br,
                    ),
                    villaFour = getRoomParams(
                        minPricePerM2 = airtableDto.minPricePerM2Villa,
                        maxPricePerM2 = airtableDto.maxPricePerM2Villa,
                        avgPricePerM2 = airtableDto.avgPricePerM2Villa,
                        minTotalPrice = airtableDto.minTotalPriceVilla4Br,
                        maxTotalPrice = airtableDto.maxTotalPriceVilla4Br,
                        avgTotalPrice = airtableDto.avgTotalPriceVilla,
                        minArea = airtableDto.minAreaVilla4Br,
                        maxArea = airtableDto.maxAreaVilla4Br,
                    ),
                    villaFive = getRoomParams(
                        minPricePerM2 = airtableDto.minPricePerM2Villa,
                        maxPricePerM2 = airtableDto.maxPricePerM2Villa,
                        avgPricePerM2 = airtableDto.avgPricePerM2Villa,
                        minTotalPrice = airtableDto.minTotalPriceVilla5Br,
                        maxTotalPrice = airtableDto.maxTotalPriceVilla5Br,
                        avgTotalPrice = airtableDto.avgTotalPriceVilla,
                        minArea = airtableDto.minAreaVilla5Br,
                        maxArea = airtableDto.maxAreaVilla5Br,
                    )
                ), // todo Кирилл сказал заполнит
                paymentPlan = airtableDto.paymentPlan,
                toolTip1 = airtableDto.salesLeaders,
                toolTip2 = airtableDto.platformSelection,
                toolTip3 = airtableDto.brokerSelection,
                lat = airtableDto.latitude,
                lon = airtableDto.longitude,
                size = airtableDto.size,
                furniture = airtableDto.furniturePackage,
                parkingPrice = airtableDto.parkingPrice,
                parkingPurchaseMethod = when (airtableDto.parkingPurchaseMethod) {
                    "Только с квартирой" -> ParkingPurchaseMethod.ONLY_APARTMENT
                    "Можно без квартиры" -> ParkingPurchaseMethod.WITHOUT_APARTMENT
                    else -> null
                },
                managementCompanyName = airtableDto.managementCompanyName,
                serviceCost = airtableDto.serviceCost,
                livingConditions = airtableDto.livingConditions,
                bookingConditions = airtableDto.bookingConditions,
                incomeDistributionMethod = airtableDto.incomeDistributionMethod
            )
        )
    }

    private fun getRoomParams(
        minPricePerM2: String?,
        maxPricePerM2: String?,
        avgPricePerM2: String?,
        minTotalPrice: String?,
        maxTotalPrice: String?,
        avgTotalPrice: String?,
        minArea: String?,
        maxArea: String?,
    ): RoomParams? {
        val pricePerMeter = getMinMaxAvgParam(minPricePerM2, maxPricePerM2, avgPricePerM2)
        val price = getMinMaxAvgParam(minTotalPrice, maxTotalPrice, avgTotalPrice)
        val square = getMinMaxAvgParam(minArea, maxArea)

        if (price == null && square == null) {
            return null
        }

        return RoomParams(
            pricePerMeter = pricePerMeter,
            price = price,
            square = square,
        )
    }

    private fun getMinMaxAvgParam(min: String?, max: String?, avg: String? = null): MinMaxAvgParam? {
        val min = min?.replace(" ", "")?.nullIfEmpty()?.let {
            if (it == "0") null
            else it.getBigDecimal()
        }
        val max = max?.replace(" ", "")?.nullIfEmpty()?.let {
            if (it == "0") null
            else it.getBigDecimal()
        }
        val avg = avg?.replace(" ", "")?.nullIfEmpty()?.let {
            if (it == "0") null else it.getBigDecimal()
        }

        if (min == null || max == null) {
            return null
        }
        return MinMaxAvgParam(
            min = min,
            max = max,
            avg  = avg
        )
    }

    private fun getMinPrice(airtableDto: AirtableProjectFieldsDto): BigDecimal {
        val defaultMin = BigDecimal.valueOf(1000000000)
        val prices = listOf(
            airtableDto.minTotalPriceStudio?.getBigDecimal(),
            airtableDto.minTotalPrice1Br?.getBigDecimal(),
            airtableDto.minTotalPrice2Br?.getBigDecimal(),
            airtableDto.minTotalPrice3Br?.getBigDecimal(),
            airtableDto.minTotalPrice4Br?.getBigDecimal(),
            airtableDto.minTotalPrice5Br?.getBigDecimal(),
            airtableDto.minTotalPriceVilla2Br?.getBigDecimal(),
            airtableDto.minTotalPriceVilla3Br?.getBigDecimal(),
            airtableDto.minTotalPriceVilla4Br?.getBigDecimal(),
            airtableDto.minTotalPriceVilla5Br?.getBigDecimal(),
            airtableDto.minPriceVillas?.getBigDecimal(),
        )

        return (prices
            .filterNotNull()
            .minOrNull()
            ?: defaultMin).scaleAndRoundAmount()
    }

    private fun getMaxPrice(airtableDto: AirtableProjectFieldsDto): BigDecimal {
        val defaultMax = BigDecimal.valueOf(-1)
        val prices = listOf(
            airtableDto.maxTotalPriceStudio?.getBigDecimal(),
            airtableDto.maxTotalPrice1Br?.getBigDecimal(),
            airtableDto.maxTotalPrice2Br?.getBigDecimal(),
            airtableDto.maxTotalPrice3Br?.getBigDecimal(),
            airtableDto.maxTotalPrice4Br?.getBigDecimal(),
            airtableDto.maxTotalPrice5Br?.getBigDecimal(),
            airtableDto.maxTotalPriceVilla2Br?.getBigDecimal(),
            airtableDto.maxTotalPriceVilla3Br?.getBigDecimal(),
            airtableDto.maxTotalPriceVilla4Br?.getBigDecimal(),
            airtableDto.maxTotalPriceVilla5Br?.getBigDecimal(),
            airtableDto.maxPriceVillas?.getBigDecimal(),
        )

        return (prices
            .filterNotNull()
            .maxOrNull()
            ?: defaultMax).scaleAndRoundAmount()

    }
}

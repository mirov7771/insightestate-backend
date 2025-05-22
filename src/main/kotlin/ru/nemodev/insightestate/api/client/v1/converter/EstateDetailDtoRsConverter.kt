package ru.nemodev.insightestate.api.client.v1.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDetailDtoRs
import ru.nemodev.insightestate.config.property.AppProperties
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.insightestate.entity.MinMaxAvgParam
import ru.nemodev.insightestate.entity.RoomParams
import ru.nemodev.insightestate.entity.TravelTime
import ru.nemodev.platform.core.extensions.isNotNullOrEmpty
import ru.nemodev.platform.core.integration.s3.minio.config.S3MinioProperties
import java.time.format.DateTimeFormatter

@Component
class EstateDetailDtoRsConverter(
    appProperties: AppProperties,
    s3MinioProperties: S3MinioProperties
) : Converter<EstateEntity, EstateDetailDtoRs> {

    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    private val baseEstateImageUrl = "${appProperties.imageBaseUrl}/${s3MinioProperties.bucket}"

    override fun convert(source: EstateEntity): EstateDetailDtoRs {
        val estateDetail = source.estateDetail
        return EstateDetailDtoRs(
            id = source.id,
            projectId = estateDetail.projectId,
            name = estateDetail.name,
            shortDescriptionRu = estateDetail.shortDescriptionRu,
            shortDescriptionEn = estateDetail.shortDescriptionEn,
            landPurchased = estateDetail.landPurchased,
            eiaEnabled = estateDetail.eiaEnabled,
            developer = EstateDetailDtoRs.EstateDeveloperDto(
                name = estateDetail.developer.name,
                country = estateDetail.developer.country,
                yearOfFoundation = estateDetail.developer.yearOfFoundation,
            ),
            grade = EstateDetailDtoRs.EstateGradeDto(
                main = estateDetail.grade.main,
                investmentSecurity = estateDetail.grade.investmentSecurity,
                investmentPotential = estateDetail.grade.investmentPotential,
                projectLocation = estateDetail.grade.projectLocation,
                comfortOfLife = estateDetail.grade.comfortOfLife,
            ),
            projectCount = EstateDetailDtoRs.ProjectCountDto(
                total = estateDetail.projectCount.total,
                build = estateDetail.projectCount.build,
                finished = estateDetail.projectCount.finished,
                deviationFromDeadline = estateDetail.projectCount.deviationFromDeadline,
            ),
            status = estateDetail.status,
            saleStartDate = estateDetail.saleStartDate?.format(dateTimeFormatter),
            buildEndDate = estateDetail.buildEndDate?.format(dateTimeFormatter),
            unitCount = EstateDetailDtoRs.UnitCountDto(
                total = estateDetail.unitCount.total,
                sailed = estateDetail.unitCount.sailed,
                available = estateDetail.unitCount.available,
            ),
            type = estateDetail.type,
            level = estateDetail.level,
            product = estateDetail.product,
            profitability = EstateDetailDtoRs.EstateProfitabilityDto(
                roi = estateDetail.profitability.roi,
                roiSummary = estateDetail.profitability.roiSummary,
                irr = estateDetail.profitability.irr,
                capRateFirstYear = estateDetail.profitability.capRateFirstYear,
            ),
            location = EstateDetailDtoRs.EstateLocationDto(
                name = estateDetail.location.name,
                district = estateDetail.location.district,
                beach = estateDetail.location.beach,
                mapUrl = estateDetail.location.mapUrl,
            ),
            infrastructure = EstateDetailDtoRs.EstateInfrastructureDto(
                beachTime = estateDetail.infrastructure.beachTime.toTravelTimeDto(),
                airportTime = estateDetail.infrastructure.airportTime.toTravelTimeDto(),
                mallTime = estateDetail.infrastructure.mallTime.toTravelTimeDto(),
                schoolRadius = estateDetail.infrastructure.school.radius,
                school = EstateDetailDtoRs.SchoolDto(
                    radius = estateDetail.infrastructure.school.radius,
                    name = estateDetail.infrastructure.school.name
                )
            ),
            options = EstateDetailDtoRs.EstateOptionsDto(
                parkingSize = estateDetail.options.parkingSize,
                gym = estateDetail.options.gym,
                childRoom = estateDetail.options.childRoom,
                shop = estateDetail.options.shop,
                entertainment = estateDetail.options.entertainment,
                coworking = estateDetail.options.coworking,
                petFriendly = estateDetail.options.petFriendly
            ),
            managementCompany = EstateDetailDtoRs.ManagementCompany(
                enabled = estateDetail.managementCompany.enabled
            ),
            price = estateDetail.price.toMinMaxAvgParamDto(),
            ceilingHeight = estateDetail.ceilingHeight,
            floors = estateDetail.floors,
            roomLayouts = EstateDetailDtoRs.RoomLayoutsDto(
                studio = estateDetail.roomLayouts.studio?.toRoomParamsDto(),
                one = estateDetail.roomLayouts.one?.toRoomParamsDto(),
                two = estateDetail.roomLayouts.two?.toRoomParamsDto(),
                three = estateDetail.roomLayouts.three?.toRoomParamsDto(),
                four = estateDetail.roomLayouts.four?.toRoomParamsDto(),
                five = estateDetail.roomLayouts.five?.toRoomParamsDto(),
                villaTwo = estateDetail.roomLayouts.villaTwo?.toRoomParamsDto(),
                villaThree = estateDetail.roomLayouts.villaThree?.toRoomParamsDto(),
                villaFour = estateDetail.roomLayouts.villaFour?.toRoomParamsDto(),
                villaFive = estateDetail.roomLayouts.villaFive?.toRoomParamsDto()
            ),
            facilityImages = estateDetail.facilityImages?.map { "$baseEstateImageUrl/$it" }?.ifEmpty { null },
            exteriorImages = estateDetail.exteriorImages?.map { "$baseEstateImageUrl/$it" }?.ifEmpty { null },
            interiorImages = estateDetail.interiorImages?.map { "$baseEstateImageUrl/$it" }?.ifEmpty { null },
            paymentPlan = estateDetail.paymentPlan,
            paymentPlanList = if (estateDetail.paymentPlan.isNotNullOrEmpty() && estateDetail.paymentPlan!!.contains(","))
                estateDetail.paymentPlan.split(",")
            else
                null,
            likes = estateDetail.likesCount
        )
    }
}

private fun TravelTime.toTravelTimeDto(): EstateDetailDtoRs.TravelTimeDto {
    return EstateDetailDtoRs.TravelTimeDto(
        walk = walk,
        car = car
    )
}

private fun MinMaxAvgParam.toMinMaxAvgParamDto(): EstateDetailDtoRs.MinMaxAvgParamDto {
    return EstateDetailDtoRs.MinMaxAvgParamDto(
        min = min,
        max = max,
        avg = avg
    )
}

private fun RoomParams.toRoomParamsDto(): EstateDetailDtoRs.RoomParamsDto? {
    if (pricePerMeter == null && price == null && square == null) {
        return null
    }
    return EstateDetailDtoRs.RoomParamsDto(
        pricePerMeter = pricePerMeter?.toMinMaxAvgParamDto(),
        price = price?.toMinMaxAvgParamDto(),
        square = square?.toMinMaxAvgParamDto(),
    )
}

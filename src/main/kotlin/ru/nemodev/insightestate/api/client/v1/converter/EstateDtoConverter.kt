package ru.nemodev.insightestate.api.client.v1.converter

import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDetailDtoRs
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDtoRs
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.insightestate.entity.MinMaxAvgParam
import ru.nemodev.insightestate.entity.RoomParams
import ru.nemodev.insightestate.entity.TravelTime
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

fun EstateEntity.toDtoRs(): EstateDtoRs {
    return EstateDtoRs(
        id = id,
        projectId = estateDetail.projectId,
        name = estateDetail.name,
        grade = estateDetail.grade.final,
        priceMin = estateDetail.price.min,
        roi = estateDetail.profitability.roi,
        buildEndDate = estateDetail.buildEndDate?.format(dateTimeFormatter) ?: "-",
        level = estateDetail.level,
        beachTravelTime = estateDetail.infrastructure.beachTime.walk ?: 0,
        images = emptyList(),
    )
}

fun EstateEntity.toDetailDtoRs(): EstateDetailDtoRs {
    return EstateDetailDtoRs(
        id = id,
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
            final = estateDetail.grade.final,
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
        saleDate = estateDetail.saleDate?.format(dateTimeFormatter) ?: "-",
        buildEndDate = estateDetail.buildEndDate?.format(dateTimeFormatter) ?: "-",
        unitCount = EstateDetailDtoRs.UnitCountDto(
            total = estateDetail.unitCount.total,
            sailed = estateDetail.unitCount.sailed,
            available = estateDetail.unitCount.available,
        ),
        constructionSchedule = estateDetail.constructionSchedule,
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
            schoolRadius = estateDetail.infrastructure.schoolRadius,
            nurserySchoolRadius = estateDetail.infrastructure.nurserySchoolRadius,
        ),
        options = EstateDetailDtoRs.EstateOptionsDto(
            parkingSize = estateDetail.options.parkingSize,
            gym = estateDetail.options.gym,
            childRoom = estateDetail.options.childRoom,
            shop = estateDetail.options.shop,
            entertainment = estateDetail.options.entertainment,
            coWorking = estateDetail.options.coWorking,
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
        images = emptyList()
    )
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

private fun RoomParams.toRoomParamsDto(): EstateDetailDtoRs.RoomParamsDto {
    return EstateDetailDtoRs.RoomParamsDto(
        pricePerMeter = pricePerMeter.toMinMaxAvgParamDto(),
        price = price.toMinMaxAvgParamDto(),
        square = square.toMinMaxAvgParamDto(),
    )
}
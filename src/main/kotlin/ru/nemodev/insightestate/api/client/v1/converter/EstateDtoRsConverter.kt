package ru.nemodev.insightestate.api.client.v1.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDtoRs
import ru.nemodev.insightestate.config.property.AppProperties
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.platform.core.integration.s3.minio.config.S3MinioProperties

@Component
class EstateDtoRsConverter(
    appProperties: AppProperties,
    s3MinioProperties: S3MinioProperties
) : Converter<EstateEntity, EstateDtoRs> {
    private val baseEstateImageUrl = "${appProperties.imageBaseUrl}/${s3MinioProperties.bucket}"

    override fun convert(source: EstateEntity): EstateDtoRs {
        val estateDetail = source.estateDetail
        val beachTravelTimeWalk = estateDetail.infrastructure.beachTime.walk ?: 0
        val beachTravelTimeCar = estateDetail.infrastructure.beachTime.car
        var beachTravelTime = beachTravelTimeCar
        if (beachTravelTimeWalk in 1..<beachTravelTimeCar)
            beachTravelTime = beachTravelTimeWalk
        return EstateDtoRs(
            id = source.id,
            projectId = estateDetail.projectId,
            name = estateDetail.name,
            grade = estateDetail.grade.main,
            priceMin = estateDetail.price.min,
            roi = estateDetail.profitability.roi,
            buildEndDate = formatDate(estateDetail.buildEndDate?.toString()),
            level = estateDetail.level,
            beachTravelTime = beachTravelTime,
            facilityImages = estateDetail.facilityImages?.map { "$baseEstateImageUrl/$it" }?.ifEmpty { null },
            exteriorImages = estateDetail.exteriorImages?.map { "$baseEstateImageUrl/$it" }?.ifEmpty { null },
            interiorImages = estateDetail.interiorImages?.map { "$baseEstateImageUrl/$it" }?.ifEmpty { null },
            roiSummary = estateDetail.profitability.roiSummary,
            city = estateDetail.location.city,
            beach = estateDetail.location.beach,
            beachTravelTimeCar = beachTravelTimeCar,
            beachTravelTimeWalk = beachTravelTimeWalk,
            toolTip1 = if (estateDetail.toolTip1.isNullOrEmpty()) "false" else "true",
            toolTip2 = if (estateDetail.toolTip2.isNullOrEmpty()) "false" else "true",
            toolTip3 = if (estateDetail.toolTip3.isNullOrEmpty()) "false" else "true",
            collectionCount = estateDetail.collectionCount ?: 0,
            lat = estateDetail.lat,
            lon = estateDetail.lon,
        )
    }
}

fun formatDate(date: String?): String {
    if (date == null)
        return ""
    val dates = date.split("-")
    if (dates.size < 3)
        return ""
    val year = dates[0]
    val month = dates[1]
    val quarter = when (month) {
        "01", "02", "03" -> "Q1"
        "04", "05", "06" -> "Q2"
        "07", "08", "09" -> "Q3"
        else -> "Q4"
    }
    return "$quarter $year"
}

package ru.nemodev.insightestate.api.client.v1.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.dto.estate.EstateDtoRs
import ru.nemodev.insightestate.config.property.AppProperties
import ru.nemodev.insightestate.entity.EstateEntity
import ru.nemodev.platform.core.integration.s3.minio.config.S3MinioProperties
import java.time.format.DateTimeFormatter

@Component
class EstateDtoRsConverter(
    appProperties: AppProperties,
    s3MinioProperties: S3MinioProperties
) : Converter<EstateEntity, EstateDtoRs> {

    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

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
            buildEndDate = estateDetail.buildEndDate ?: "",
            level = estateDetail.level,
            beachTravelTime = beachTravelTime,
            facilityImages = estateDetail.facilityImages?.map { "$baseEstateImageUrl/$it" }?.ifEmpty { null },
            exteriorImages = estateDetail.exteriorImages?.map { "$baseEstateImageUrl/$it" }?.ifEmpty { null },
            interiorImages = estateDetail.interiorImages?.map { "$baseEstateImageUrl/$it" }?.ifEmpty { null },
            roiSummary = estateDetail.profitability.roiSummary,
            city = estateDetail.location.city,
            beachTravelTimeCar = beachTravelTimeCar,
            beachTravelTimeWalk = beachTravelTimeWalk,
            toolTip1 = estateDetail.toolTip1,
            toolTip2 = estateDetail.toolTip2,
            toolTip3 = estateDetail.toolTip3,
        )
    }
}

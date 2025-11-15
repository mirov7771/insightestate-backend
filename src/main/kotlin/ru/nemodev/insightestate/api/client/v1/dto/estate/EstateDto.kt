package ru.nemodev.insightestate.api.client.v1.dto.estate

import ru.nemodev.insightestate.entity.*
import java.math.BigDecimal
import java.util.*

data class EstateDtoRs(
    val id: UUID,
    val projectId: String,
    val name: String,
    val grade: BigDecimal,
    val priceMin: BigDecimal,
    val roi: BigDecimal,
    val buildEndDate: String,
    val level: EstateLevelType,
    val beachTravelTime: Int,
    var facilityImages: List<String>?,
    var exteriorImages: List<String>?,
    var interiorImages: List<String>?,
    var roiSummary: BigDecimal?,
    var city: String,
    var beach: String?,
    var beachTravelTimeCar: Int?,
    var beachTravelTimeWalk: Int?,
    var toolTip1: String?,
    var toolTip2: String?,
    var toolTip3: String?,
    var collectionCount: Int? = null,
    var lat: String? = null,
    var lon: String? = null,
)

data class EstateDetailDtoRs(
    val id: UUID,
    val projectId: String,
    val name: String,
    val shortDescriptionRu: String?,
    val shortDescriptionEn: String?,

    val landPurchased: Boolean,
    val eiaEnabled: Boolean,
    val developer: EstateDeveloperDto,
    val grade: EstateGradeDto,
    val projectCount: ProjectCountDto,
    val status: EstateStatus,
    val saleStartDate: String?,
    val buildEndDate: String?,
    val unitCount: UnitCountDto,

    var type: EstateType,
    val level: EstateLevelType,
    val product: EstateProductType,
    val profitability: EstateProfitabilityDto,

    val location: EstateLocationDto,
    val infrastructure: EstateInfrastructureDto,
    val options: EstateOptionsDto,
    val managementCompany: ManagementCompany,

    val price: MinMaxAvgParamDto,
    val ceilingHeight: BigDecimal?,
    val floors: Int?,
    val roomLayouts: RoomLayoutsDto,

    var facilityImages: List<String>?,
    var exteriorImages: List<String>?,
    var interiorImages: List<String>?,
    val paymentPlan: String?,
    val paymentPlanList: List<String>?,
    val likes: Long? = null,
    var city: String,
    var toolTip1: String?,
    var toolTip2: String?,
    var toolTip3: String?,

    var units: List<UnitEntity>? = null,
    val lat: String? = null,
    val lon: String? = null,

    val priceDate: String? = null,
) {
    data class EstateGradeDto(
        val main: BigDecimal,
        val investmentSecurity: BigDecimal,
        val investmentPotential: BigDecimal,
        val projectLocation: BigDecimal,
        val comfortOfLife: BigDecimal,
    )

    data class EstateDeveloperDto(
        val name: String,
        val country: String?,
        val yearOfFoundation: Int?,
        val phone: String? = null,
        val email: String? = null,
        val presentation: Boolean
    )

    data class ProjectCountDto(
        val total: Int,
        val build: Int,
        val finished: Int,
        val deviationFromDeadline: Int?,
    )

    data class UnitCountDto(
        val total: Int,
        val sailed: Int?,
        val available: Int?
    )

    data class EstateLocationDto(
        val name: String,
        val district: String,
        val beach: String,
        val mapUrl: String,
    )

    data class EstateInfrastructureDto(
        val beachTime: TravelTimeDto,
        val airportTime: TravelTimeDto,
        val mallTime: TravelTimeDto,
        @Deprecated("Использовать school")
        val schoolRadius: BigDecimal,
        val school: SchoolDto
    )

    data class TravelTimeDto(
        val walk: Int?,
        val car: Int,
    )

    data class SchoolDto(
        val radius: BigDecimal,
        val name: String?,
    )

    data class EstateProfitabilityDto(
        val roi: BigDecimal,
        val roiSummary: BigDecimal,
        val irr: BigDecimal,
        val capRateFirstYear: BigDecimal,
    )

    data class EstateOptionsDto(
        val parkingSize: Int?,
        val gym: Boolean,
        val childRoom: Boolean,
        val shop: Boolean,
        val entertainment: Boolean,
        val coworking: Boolean,
        val petFriendly: Boolean
    )

    data class ManagementCompany(
        val enabled: Boolean,
    )

    data class RoomLayoutsDto(
        val studio: RoomParamsDto?,
        val one: RoomParamsDto?,
        val two: RoomParamsDto?,
        val three: RoomParamsDto?,
        val four: RoomParamsDto?,
        val five: RoomParamsDto?,
        val villaTwo: RoomParamsDto?,
        val villaThree: RoomParamsDto?,
        val villaFour: RoomParamsDto?,
        val villaFive: RoomParamsDto?,
    )

    data class RoomParamsDto(
        val pricePerMeter: MinMaxAvgParamDto?,
        val price: MinMaxAvgParamDto?,
        val square: MinMaxAvgParamDto?,
    )

    data class MinMaxAvgParamDto (
        val min: String?,
        val max: String?,
        val avg: String?,
    )
}

data class AiRequest (
    val request: String
)

data class GeoRs(
    val geo: List<GeoDto>?,
)

data class GeoDto (
    val id: UUID,
    val lat: String?,
    val lng: String?,
    val title: String?,
    val image: String?,
    val description: String,
    val toolTip1: String?,
    val toolTip2: String?,
    val toolTip3: String?,
    val roi: String?,
)

data class UnitsRs (
    val id: UUID,
    val name: String,
    val images: List<String>?,
    val items: List<UnitEntity>? = null,
    val count: Int,
)

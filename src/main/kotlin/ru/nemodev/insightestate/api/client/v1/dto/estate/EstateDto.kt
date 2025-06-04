package ru.nemodev.insightestate.api.client.v1.dto.estate

import ru.nemodev.insightestate.entity.EstateLevelType
import ru.nemodev.insightestate.entity.EstateProductType
import ru.nemodev.insightestate.entity.EstateStatus
import ru.nemodev.insightestate.entity.EstateType
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
    var beachTravelTimeCar: Int?,
    var beachTravelTimeWalk: Int?,
    var toolTip1: String?,
    var toolTip2: String?,
    var toolTip3: String?,
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
        val min: BigDecimal,
        val max: BigDecimal,
        val avg: BigDecimal?,
    )
}

data class AiRequest (
    val request: String
)

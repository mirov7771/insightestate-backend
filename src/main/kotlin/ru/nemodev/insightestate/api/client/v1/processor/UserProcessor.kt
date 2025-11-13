package ru.nemodev.insightestate.api.client.v1.processor


import org.dhatim.fastexcel.ConditionalFormattingExpressionRule
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import ru.nemodev.insightestate.api.client.v1.dto.user.*
import ru.nemodev.insightestate.repository.EstateCollectionRepository
import ru.nemodev.insightestate.repository.TariffRepository
import ru.nemodev.insightestate.service.UserService
import ru.nemodev.insightestate.service.subscription.SubscriptionService
import java.io.ByteArrayOutputStream
import java.util.*

interface UserProcessor {
    fun getUser(authBasicToken: String): UserDtoRs
    fun update(authBasicToken: String, request: UserUpdateDtoRq)
    fun helpWithClient(authBasicToken: String, request: HelpWithClientRq)
    fun deleteUser(authBasicToken: String)
    fun theme(request: ThemeDto)
    fun addToGroup(rq: UserGroupDto)
    fun report(): ResponseEntity<InputStreamResource>
}

@Component
class UserProcessorImpl(
    private val userService: UserService,
    private val estateCollectionRepository: EstateCollectionRepository,
    private val tariffRepository: TariffRepository,
    private val subscriptionService: SubscriptionService
) : UserProcessor {

    companion object {
        private val EXCEL_HEADERS = listOf(
            "ФИО",
            "Email",
            "Телефон",
            "Последняя активность",
            "Группа",
            "Подборки",
            "Тариф"
        )
    }

    override fun getUser(authBasicToken: String): UserDtoRs {
        val userEntity = userService.getUser(authBasicToken)
        val estateCollections = estateCollectionRepository.findAllByParams(
            userId = userEntity.id.toString(),
            limit = 50,
            offset = 0
        )
        return UserDtoRs(
            login = userEntity.userDetail.login,
            fio = userEntity.userDetail.fio!!,
            mobileNumber = userEntity.userDetail.mobileNumber!!,
            location = userEntity.userDetail.location!!,
            whatsUp = userEntity.userDetail.whatsUp,
            tgName = userEntity.userDetail.tgName,
            profileImage = userEntity.userDetail.profileImage,
            id = userEntity.id,
            group = userEntity.userDetail.group,
            collectionLogo = userEntity.userDetail.collectionLogo,
            collectionColorId = userEntity.userDetail.collectionColorId,
            collectionColorValue = userEntity.userDetail.collectionColorValue,
            collectionCount = estateCollections.size
        )
    }

    override fun update(authBasicToken: String, request: UserUpdateDtoRq) {
        userService.update(authBasicToken, request)
    }

    override fun helpWithClient(authBasicToken: String, request: HelpWithClientRq) {
        userService.helpWithClient(authBasicToken, request)
    }

    override fun deleteUser(authBasicToken: String) {
        userService.deleteUser(authBasicToken)
    }

    override fun theme(request: ThemeDto) {
        userService.updateTheme(
            userId = UUID.fromString(request.userId),
            logo = request.logo,
            colorId = request.colorId,
            colorValue = request.colorValue,
        )
    }

    override fun addToGroup(rq: UserGroupDto) {
        val user = userService.findByLogin(rq.email) ?: return
        if (rq.group != null) {
            user.userDetail.group = rq.group.name
            userService.update(user)
        }
        if (rq.tariff != null) {
            val tariff = tariffRepository.findByTitle(rq.tariff.name) ?: return
            subscriptionService.saveTariff(
                userId = user.id,
                tariffId = tariff.id
            )
        }
    }

    override fun report(): ResponseEntity<InputStreamResource> {
        val report = tariffRepository.getReport()
        val csvResult = ByteArrayOutputStream()
        val wb = Workbook(csvResult, "MyApplication", "1.0")
        val ws: Worksheet = wb.newWorksheet("report")
        ws.value(0, 0, "report")
        EXCEL_HEADERS.forEachIndexed { index, header ->
            ws.value(2, index, header) // заголовки в строке 3 (индекс 2)
            ws.style(2, index).fillColor("FF8800").set(ConditionalFormattingExpressionRule("LENB(A1)>1", true))
        }
        val keys = listOf(9, 13, 14, 15, 16, 17, 20)
        report.forEachIndexed { rowIndex, row ->
            keys.forEachIndexed { colIndex, key ->
                val value = when (key) {
                    9 -> row.fio
                    13 -> row.login
                    14 -> row.mobile
                    15 -> row.lastDate.toString().replace("T", " ").substringBefore(".")
                    16 -> row.groupName
                    17 -> row.collections.toString()
                    else -> row.tariff
                }
                ws.value(rowIndex + 3, colIndex, value)
            }
        }
        wb.finish()

        return ResponseEntity.status(HttpStatus.OK)
            .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"report.xlsx\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .contentType(MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(InputStreamResource(csvResult.toByteArray().inputStream(), "report.xlsx"))
    }
}

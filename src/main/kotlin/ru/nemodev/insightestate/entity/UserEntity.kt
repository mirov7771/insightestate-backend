package ru.nemodev.insightestate.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import ru.nemodev.platform.core.db.annotation.StoreJson
import ru.nemodev.platform.core.db.entity.AbstractEntity
import java.time.LocalDateTime
import java.util.*

@Table("users")
class UserEntity(
    id: UUID? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = createdAt,

    @Column("user_detail")
    val userDetail: UserDetail
) : AbstractEntity<UUID>(id, createdAt, updatedAt), UserDetails {

    companion object {
        private val userAuthorities = AuthorityUtils.createAuthorityList("USER")
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = userAuthorities
    override fun getPassword(): String? = userDetail.passwordHash
    override fun getUsername() = userDetail.login
}

@StoreJson
data class UserDetail(
    val login: String,
    var passwordHash: String? = null,
    var signUpConfirmCode: String,
    var fio: String? = null,
    var mobileNumber: String? = null,
    var location: String? = null,
    var status: UserStatus,
    var whatsUp: String? = null,
    var tgName: String? = null,
    var profileImage: String? = null,
)

enum class UserStatus {
    SIGN_UP_CONFIRM_CODE,
    SIGN_UP_CONFIRMED,
    ACTIVE;

    fun isSignUpConfirmCode() = this == SIGN_UP_CONFIRM_CODE
    fun isSignUpConfirmed() = this == SIGN_UP_CONFIRMED
    fun isActive() = this == ACTIVE
}

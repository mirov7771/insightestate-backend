package ru.nemodev.insightestate.extension

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import ru.nemodev.platform.core.extensions.fileExtensionToContentTypeMap
import ru.nemodev.platform.core.extensions.parseBasicAuth
import java.math.RoundingMode

val imageSupportedExtensions = setOf(
    "jpeg",
    "png"
)

fun String.isSupportImageFileExtension(): Boolean {
    return this in imageSupportedExtensions
}

fun String.getFileContentType(): String {
    return fileExtensionToContentTypeMap[this]
        ?: throw IllegalArgumentException("File extension $this not supported")
}

fun String.toAuthenticationToken(): Authentication {
    val loginAndPassword = this.parseBasicAuth()
    return UsernamePasswordAuthenticationToken(
        loginAndPassword.first,
        loginAndPassword.second
    )
}

fun String?.toRoundedString(
    scale: Int = 0,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
): String? =
    this?.toBigDecimal()?.setScale(scale, roundingMode)?.toString()

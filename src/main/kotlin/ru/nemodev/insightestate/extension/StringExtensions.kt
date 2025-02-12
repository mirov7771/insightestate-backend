package ru.nemodev.insightestate.extension

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import ru.nemodev.platform.core.extensions.fileExtensionToContentTypeMap
import ru.nemodev.platform.core.extensions.parseBasicAuth

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
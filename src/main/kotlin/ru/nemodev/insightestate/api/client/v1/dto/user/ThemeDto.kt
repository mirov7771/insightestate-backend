package ru.nemodev.insightestate.api.client.v1.dto.user

data class ThemeDto (
    val userId: String,
    val logo: String? = null,
    val colorId: String? = null,
    val colorValue: String? = null
)

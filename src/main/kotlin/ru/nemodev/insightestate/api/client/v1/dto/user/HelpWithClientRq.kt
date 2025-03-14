package ru.nemodev.insightestate.api.client.v1.dto.user

data class HelpWithClientRq (
    val name: String,
    val lastName: String,
    val phone: String,
    val objectName: String,
    val objectId: String,
    val location: String
)

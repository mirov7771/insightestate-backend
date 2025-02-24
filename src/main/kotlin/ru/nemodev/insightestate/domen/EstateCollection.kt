package ru.nemodev.insightestate.domen

import ru.nemodev.insightestate.entity.EstateCollectionEntity
import ru.nemodev.insightestate.entity.EstateEntity

data class EstateCollection(
    val estateCollection: EstateCollectionEntity,
    val estates: List<EstateEntity>
)

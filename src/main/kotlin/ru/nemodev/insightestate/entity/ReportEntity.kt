package ru.nemodev.insightestate.entity

import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDate
import java.time.LocalDateTime

data class ReportEntity (
    @Column("fio")
    val fio: String? = null,
    @Column("login")
    val login: String? = null,
    @Column("mobile")
    val mobile: String? = null,
    @Column("last_date")
    val lastDate: LocalDateTime,
    @Column("group_name")
    val groupName: String? = null,
    @Column("collections")
    val collections: Int? = null,
    @Column("tariff")
    val tariff: String? = null,
)

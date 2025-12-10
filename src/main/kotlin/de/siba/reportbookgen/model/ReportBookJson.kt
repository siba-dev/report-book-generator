package de.siba.reportbookgen.model

import kotlinx.serialization.Serializable

@Serializable
data class ReportBookWeekJson(
    var number: Int? = null,
    val activity: List<String>,
    val activity_hours: Int,
    val teachings: List<String>,
    val teachings_hours: Int,
    val school: SchoolSubjectsJson,
    val school_days: Int
)

@Serializable
data class SchoolSubjectsJson(
    val awp: String = "",
    val itt: String = "",
    val its: String = "",
    val d: String = "",
    val e: String = "",
    val eth: String = "",
    val pug: String = "",
    val bp: String = "",
    val misc: String = ""
)

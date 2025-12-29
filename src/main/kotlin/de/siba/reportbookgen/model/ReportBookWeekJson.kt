package de.siba.reportbookgen.model

import kotlinx.serialization.Serializable

@Serializable
data class ReportBookWeekJson(
    var number: Int? = null,
    val activity: List<String>,
    val activity_hours: Int,
    val teachings: List<String>,
    val teachings_hours: Int,
    val school: Map<String, String>,
    val school_days: Int
)
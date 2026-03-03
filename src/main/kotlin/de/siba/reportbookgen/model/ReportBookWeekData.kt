package de.siba.reportbookgen.model

import kotlinx.datetime.LocalDate

data class ReportBookWeekData(
    val weekNumber: Int,
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val year: Int,
    val activity: List<String>,
    val activityHours: Int,
    val teachings: List<String>,
    val teachingsHours: Int,
    val school: Map<String, String>,
    val schoolHours: Int
)
